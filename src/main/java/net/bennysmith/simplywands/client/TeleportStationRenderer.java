package net.bennysmith.simplywands.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.bennysmith.simplywands.blockentity.PedestalBlockEntity;
import net.bennysmith.simplywands.item.ModItems;
import net.bennysmith.simplywands.network.TeleportToPedestalPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Draws every Teleport Wand pedestal in range as a Travel Anchors style waypoint: a glow outline
 * that thickens as the player closes in, with a name plate whose size is derived from distance so
 * it stays legible at any range.
 *
 * <p>Rendering happens on a level stage rather than in the block entity renderer, because a station
 * is a world marker that has to stay readable through terrain and from outside the pedestal's own
 * model. The wand itself is drawn by {@link PedestalRenderer} like every other wand; this only adds
 * the outline and name plate, so the two do not draw the same thing twice.
 */
public final class TeleportStationRenderer {
    //  Travel Anchors steps the outline width up as the player approaches so the glow reads as a
    //  halo rather than a hairline. Four tiers cover the same feel at a fraction of the cost.
    private static final RenderType THIN_LINES = createLines("thin_lines", 1);
    private static final RenderType BOLDER_LINES = createLines("bolder_lines", 2);
    private static final RenderType BOLD_LINES = createLines("bold_lines", 3);
    private static final RenderType VERY_BOLD_LINES = createLines("very_bold_lines", 5);

    //  How far a station is drawn. Not Config.teleportWandDistance: the wand range lives in the
    //  server config and a dedicated server never sends it, so a client side read would be zero.
    private static final double RENDER_RANGE = 100.0;
    //  Travel Anchors only offers a station to a player who is actually looking at it.
    private static final double TARGET_CONE_DEGREES = 30.0;
    //  Height above the pedestal that the player aims at, and where the name plate sits.
    private static final float MARKER_HEIGHT = 1.15F;
    private static final float[] MARKER_GLOW = {0.62F, 0.20F, 0.93F};

    private static BlockPos targetedStation;

    private TeleportStationRenderer() {}

    private static RenderType createLines(String name, int strength) {
        return RenderType.create("simplywands_" + name,
                DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(strength)))
                        .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                        //  Colour only: the outline must not write depth or it would punch a
                        //  hole in the terrain drawn after it.
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setCullState(RenderStateShard.NO_CULL)
                        //  Without this the frame is hidden by whatever stands between the
                        //  player and the station, which defeats the point of a waypoint.
                        .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                        .createCompositeState(false));
    }

    @SubscribeEvent
    public static void renderStations(RenderLevelStageEvent event) {
        //  Must be a stage LevelRenderer dispatches WITH a pose stack. Stages bound to a render
        //  type (AFTER_TRIPWIRE_BLOCKS, AFTER_SOLID_BLOCKS, ...) are fired from
        //  renderSectionLayer with no pose stack, and the event then hands out a fresh identity
        //  one - every station would be drawn at pos - camera from the world origin instead of
        //  from the camera. AFTER_WEATHER is dispatched with the real camera pose stack.
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;
        if (!holdsWand(minecraft)) {
            targetedStation = null;
            return;
        }

        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Vec3 camera = event.getCamera().getPosition();
        Vec3 eye = minecraft.player.getEyePosition();
        Vec3 look = minecraft.player.getLookAngle();
        double cone = Math.toRadians(TARGET_CONE_DEGREES);

        BlockPos previousTarget = targetedStation;
        targetedStation = null;
        double bestAngle = cone;
        double bestDistance = Double.MAX_VALUE;
        List<Station> visible = new ArrayList<>();

        for (PedestalBlockEntity pedestal : nearbyStations(minecraft, eye)) {
            BlockPos pos = pedestal.getBlockPos();
            Vec3 marker = Vec3.atCenterOf(pos).add(0, MARKER_HEIGHT + 0.1, 0);
            double distance = marker.distanceTo(eye);
            if (distance > RENDER_RANGE) continue;

            //  Closest station inside the look cone wins, which is how Travel Anchors decides
            //  which anchor to name in gold and scale up.
            double angle = Math.acos(Math.max(-1.0, Math.min(1.0, look.dot(marker.subtract(eye).normalize()))));
            boolean active = angle <= bestAngle && distance < bestDistance;
            if (active) {
                targetedStation = pos;
                bestAngle = angle;
                bestDistance = distance;
            }
            //  Keep last frame's target highlighted on the frame the player turns away from it,
            //  otherwise the gold name flickers off before the click can land.
            visible.add(new Station(pedestal, pos, distance, active || pos.equals(previousTarget)));
        }

        //  Two passes so each kind of geometry shares one flush. Flushing per station would
        //  drain every buffer in the renderer, up to two hundred times a frame.
        for (Station station : visible) {
            renderOutline(minecraft, pose, buffers, station, camera);
        }
        for (Station station : visible) {
            renderNamePlate(minecraft, pose, buffers, station, camera);
        }
        buffers.endBatch();
    }

    private record Station(PedestalBlockEntity pedestal, BlockPos pos, double distance, boolean emphasised) {}

    private static List<PedestalBlockEntity> nearbyStations(Minecraft minecraft, Vec3 eye) {
        List<PedestalBlockEntity> stations = new ArrayList<>();
        int centreX = minecraft.player.blockPosition().getX() >> 4;
        int centreZ = minecraft.player.blockPosition().getZ() >> 4;
        int chunkRange = (int) Math.ceil(RENDER_RANGE / 16.0);
        double rangeSq = RENDER_RANGE * RENDER_RANGE;
        for (int x = centreX - chunkRange; x <= centreX + chunkRange; x++) {
            for (int z = centreZ - chunkRange; z <= centreZ + chunkRange; z++) {
                if (!minecraft.level.hasChunk(x, z)) continue;
                for (BlockEntity entity : minecraft.level.getChunk(x, z).getBlockEntities().values()) {
                    if (entity instanceof PedestalBlockEntity pedestal
                            && pedestal.hasWand(ModItems.TELEPORT_WAND.get())
                            && pedestal.getBlockPos().distToCenterSqr(eye) <= rangeSq) {
                        stations.add(pedestal);
                    }
                }
            }
        }
        return stations;
    }

    /**
     * The glow. The line box stays locked to the pedestal's own bounds so it always frames the
     * block, while the line width and alpha carry the distance cue.
     */
    private static void renderOutline(Minecraft minecraft, PoseStack pose, MultiBufferSource.BufferSource buffers,
                                      Station station, Vec3 camera) {
        BlockPos pos = station.pos();
        double distanceSq = station.distance() * station.distance();
        RenderType type = distanceSq > 85 * 85 ? THIN_LINES
                : distanceSq > 38 * 38 ? BOLDER_LINES
                : distanceSq > 15 * 15 ? BOLD_LINES
                : VERY_BOLD_LINES;
        float alpha = station.emphasised() ? 1.0F : 0.65F;

        pose.pushPose();
        pose.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        LevelRenderer.renderLineBox(pose, buffers.getBuffer(type),
                0.0, 0.0, 0.0, 1.0, 1.0, 1.0,
                MARKER_GLOW[0], MARKER_GLOW[1], MARKER_GLOW[2], alpha);
        pose.popPose();
    }

    /**
     * The name plate. Scale is derived from distance and corrected for field of view so the text
     * covers a similar slice of screen near and far, and it is drawn twice - see-through first -
     * to get the soft halo Travel Anchors uses.
     */
    private static void renderNamePlate(Minecraft minecraft, PoseStack pose, MultiBufferSource.BufferSource buffers,
                                        Station station, Vec3 camera) {
        BlockPos pos = station.pos();
        ItemStack wand = station.pedestal().getWand();
        if (!wand.has(DataComponents.CUSTOM_NAME)) return;
        String name = wand.getHoverName().getString();

        double scale = Math.sqrt(0.0035 * Math.sqrt(station.distance() * station.distance()));
        if (scale < 0.1) scale = 0.1;
        scale *= Math.sin(Math.toRadians(minecraft.options.fov().get()) / 4.0);
        if (station.emphasised()) scale *= 1.3;
        float textScale = (float) scale;

        int colour = station.emphasised() && ChatFormatting.GOLD.getColor() != null
                ? ChatFormatting.GOLD.getColor()
                : 0xFFFFFF;
        int alpha = (int) (minecraft.options.getBackgroundOpacity(0.5F) * 255) << 24;

        pose.pushPose();
        pose.translate(pos.getX() + 0.5 - camera.x, pos.getY() + MARKER_HEIGHT + 0.55 - camera.y, pos.getZ() + 0.5 - camera.z);
        pose.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        pose.scale(textScale, -textScale, textScale);
        Matrix4f matrix = pose.last().pose();
        float halfWidth = (float) (-minecraft.font.width(name) / 2.0);

        minecraft.font.drawInBatch(name, halfWidth, 0, colour, false, matrix, buffers,
                Font.DisplayMode.SEE_THROUGH, alpha, LightTexture.FULL_BRIGHT);
        minecraft.font.drawInBatch(name, halfWidth, 0, colour, false, matrix, buffers,
                Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        pose.popPose();
    }

    private static boolean holdsWand(Minecraft minecraft) {
        return minecraft.player.getMainHandItem().is(ModItems.TELEPORT_WAND.get())
                || minecraft.player.getOffhandItem().is(ModItems.TELEPORT_WAND.get());
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getButton() != 1 || event.getAction() != 1 || minecraft.player == null
                || targetedStation == null || minecraft.screen != null
                || !holdsWand(minecraft)) return;
        PacketDistributor.sendToServer(new TeleportToPedestalPayload(targetedStation));
        event.setCanceled(true);
    }
}