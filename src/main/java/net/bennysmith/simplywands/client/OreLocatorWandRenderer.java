package net.bennysmith.simplywands.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.bennysmith.simplywands.item.custom.OreLocatorWand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

import java.awt.Color;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.concurrent.ConcurrentHashMap;

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

        ConcurrentHashMap<BlockPos, OreLocatorWand.BlockHighlight> highlightedBlocks = OreLocatorWand.getHighlightedBlocks();
        if (highlightedBlocks.isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        
        highlightedBlocks.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            return currentTime > entry.getValue().expirationTime ||
                   Minecraft.getInstance().level.getBlockState(pos).isAir();
        });

        if (!highlightedBlocks.isEmpty()) {
            renderHighlightedBlocks(event, highlightedBlocks, currentTime);
        }

        // No need to update the original map as it's the same instance
    }

    private static void renderHighlightedBlocks(RenderLevelStageEvent event, Map<BlockPos, OreLocatorWand.BlockHighlight> highlightedBlocks, long currentTime) {
        PoseStack poseStack = event.getPoseStack();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        VertexConsumer builder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(OVERLAY_LINES);

        Vec3 cameraPos = event.getCamera().getPosition();
        for (Map.Entry<BlockPos, OreLocatorWand.BlockHighlight> entry : highlightedBlocks.entrySet()) {
            renderOutline(poseStack, entry.getKey(), entry.getValue().block, cameraPos, builder, highlightedBlocks);
        }

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(OVERLAY_LINES);
        RenderSystem.enableDepthTest();
    }

    private static void renderOutline(PoseStack poseStack, BlockPos pos, Block block, Vec3 cameraPos, VertexConsumer builder, Map<BlockPos, OreLocatorWand.BlockHighlight> highlightedBlocks) {
        poseStack.pushPose();
        poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

        float minX = -0.002f, minY = -0.002f, minZ = -0.002f;
        float maxX = 1.002f, maxY = 1.002f, maxZ = 1.002f;

        Color color = getColorForBlock(block);

        BiConsumer<Vector3f, Vector3f> drawLine = (start, end) -> {
            builder.addVertex(poseStack.last().pose(), start.x(), start.y(), start.z())
                   .setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                   .setNormal(0, 1, 0);
            builder.addVertex(poseStack.last().pose(), end.x(), end.y(), end.z())
                   .setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                   .setNormal(0, 1, 0);
        };

        // Check for adjacent blocks and only draw lines if there's no neighbor
        boolean hasWest = highlightedBlocks.containsKey(pos.west());
        boolean hasEast = highlightedBlocks.containsKey(pos.east());
        boolean hasSouth = highlightedBlocks.containsKey(pos.south());
        boolean hasNorth = highlightedBlocks.containsKey(pos.north());
        boolean hasAbove = highlightedBlocks.containsKey(pos.above());
        boolean hasBelow = highlightedBlocks.containsKey(pos.below());

        // Draw horizontal lines
        if (!hasEast && !hasAbove) drawLine.accept(new Vector3f(maxX, maxY, minZ), new Vector3f(maxX, maxY, maxZ));
        if (!hasEast && !hasBelow) drawLine.accept(new Vector3f(maxX, minY, minZ), new Vector3f(maxX, minY, maxZ));
        if (!hasWest && !hasAbove) drawLine.accept(new Vector3f(minX, maxY, minZ), new Vector3f(minX, maxY, maxZ));
        if (!hasWest && !hasBelow) drawLine.accept(new Vector3f(minX, minY, minZ), new Vector3f(minX, minY, maxZ));

        if (!hasNorth && !hasAbove) drawLine.accept(new Vector3f(minX, maxY, minZ), new Vector3f(maxX, maxY, minZ));
        if (!hasNorth && !hasBelow) drawLine.accept(new Vector3f(minX, minY, minZ), new Vector3f(maxX, minY, minZ));
        if (!hasSouth && !hasAbove) drawLine.accept(new Vector3f(minX, maxY, maxZ), new Vector3f(maxX, maxY, maxZ));
        if (!hasSouth && !hasBelow) drawLine.accept(new Vector3f(minX, minY, maxZ), new Vector3f(maxX, minY, maxZ));

        // Draw vertical lines
        if (!hasEast && !hasNorth) drawLine.accept(new Vector3f(maxX, minY, minZ), new Vector3f(maxX, maxY, minZ));
        if (!hasEast && !hasSouth) drawLine.accept(new Vector3f(maxX, minY, maxZ), new Vector3f(maxX, maxY, maxZ));
        if (!hasWest && !hasNorth) drawLine.accept(new Vector3f(minX, minY, minZ), new Vector3f(minX, maxY, minZ));
        if (!hasWest && !hasSouth) drawLine.accept(new Vector3f(minX, minY, maxZ), new Vector3f(minX, maxY, maxZ));

        poseStack.popPose();

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(RenderType.lines());
    }

    private static Color getColorForBlock(Block block) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            return new Color(0, 255, 255); // Cyan for diamond
        } else if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
            return new Color(255, 0, 0); // Red for redstone
        } else if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) {
            return new Color(0, 0, 255); // Blue for lapis
        } else if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
            return new Color(255, 215, 0); // Gold for gold ore
        } else if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            return new Color(210, 105, 30); // Brown for iron ore
        } else if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) {
            return new Color(0, 255, 0); // Green for emerald
        } else if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) {
            return new Color(60, 100, 100); // Brighter dark slate gray for coal
        } else if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            return new Color(184, 115, 51); // Copper color
        } else {
            return new Color(255, 255, 255); // White for unknown ores
        }
    }
}
