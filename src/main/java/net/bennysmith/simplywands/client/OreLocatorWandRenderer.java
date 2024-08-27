package net.bennysmith.simplywands.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.bennysmith.simplywands.Config;
import net.bennysmith.simplywands.item.custom.OreLocatorWand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = "simplywands", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class OreLocatorWandRenderer {

    private static final RenderType OVERLAY_LINES = RenderType.create(
            "overlay_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(RenderStateShard.NO_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .createCompositeState(false)
    );

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        PoseStack poseStack = event.getPoseStack();
        long currentTime = System.currentTimeMillis();

        Map<BlockPos, Long> highlightedBlocks = OreLocatorWand.getHighlightedBlocks();
        highlightedBlocks.entrySet().removeIf(entry -> currentTime > entry.getValue());

        if (!highlightedBlocks.isEmpty()) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            VertexConsumer builder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(OVERLAY_LINES);

            Vec3 cameraPos = event.getCamera().getPosition();
            for (BlockPos pos : highlightedBlocks.keySet()) {
                renderOutline(poseStack, pos, cameraPos, builder);
            }
        }

        if (currentTime > OreLocatorWand.getLastHighlightTime() + Config.highlightDurationMs) {
            RenderSystem.enableDepthTest();
        }
    }

    private static void renderOutline(PoseStack poseStack, BlockPos pos, Vec3 cameraPos, VertexConsumer builder) {
        poseStack.pushPose();
        poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

        float minX = -0.002f, minY = -0.002f, minZ = -0.002f;
        float maxX = 1.002f, maxY = 1.002f, maxZ = 1.002f;

        BiConsumer<Vector3f, Vector3f> drawLine = (start, end) -> {
            builder.addVertex(poseStack.last().pose(), start.x(), start.y(), start.z()).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
            builder.addVertex(poseStack.last().pose(), end.x(), end.y(), end.z()).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        };

        drawLine.accept(new Vector3f(minX, minY, minZ), new Vector3f(maxX, minY, minZ));
        drawLine.accept(new Vector3f(maxX, minY, minZ), new Vector3f(maxX, minY, maxZ));
        drawLine.accept(new Vector3f(maxX, minY, maxZ), new Vector3f(minX, minY, maxZ));
        drawLine.accept(new Vector3f(minX, minY, maxZ), new Vector3f(minX, minY, minZ));

        drawLine.accept(new Vector3f(minX, maxY, minZ), new Vector3f(maxX, maxY, minZ));
        drawLine.accept(new Vector3f(maxX, maxY, minZ), new Vector3f(maxX, maxY, maxZ));
        drawLine.accept(new Vector3f(maxX, maxY, maxZ), new Vector3f(minX, maxY, maxZ));
        drawLine.accept(new Vector3f(minX, maxY, maxZ), new Vector3f(minX, maxY, minZ));

        drawLine.accept(new Vector3f(minX, minY, minZ), new Vector3f(minX, maxY, minZ));
        drawLine.accept(new Vector3f(maxX, minY, minZ), new Vector3f(maxX, maxY, minZ));
        drawLine.accept(new Vector3f(maxX, minY, maxZ), new Vector3f(maxX, maxY, maxZ));
        drawLine.accept(new Vector3f(minX, minY, maxZ), new Vector3f(minX, maxY, maxZ));

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(RenderType.lines());

        poseStack.popPose();
    }
}
