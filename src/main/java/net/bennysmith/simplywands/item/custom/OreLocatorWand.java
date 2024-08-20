package net.bennysmith.simplywands.item.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.Tags;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;

@EventBusSubscriber(modid = "simplywands", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class OreLocatorWand extends Item {

    // Constants for highlight radius and duration
    private static final int HIGHLIGHT_RADIUS = 32;
    private static final long HIGHLIGHT_DURATION_MS = 5000; // 30 seconds (30000)

    // Map to store highlighted blocks and their expiration times
    private static final Map<BlockPos, Long> highlightedBlocks = new HashMap<>();
    private static long lastHighlightTime = 0;

    // Custom RenderType for rendering the overlay lines
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

    public OreLocatorWand(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Check if the player is looking at a block within 5 blocks range
            HitResult hitResult = player.pick(5.0D, 0.0F, false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos pos = blockHitResult.getBlockPos();
                BlockState state = level.getBlockState(pos);
                Block targetBlock = state.getBlock();

                // If the block is an ore, highlight nearby ores of the same type
                if (targetBlock.defaultBlockState().is(Tags.Blocks.ORES)) {
                    highlightNearbyOres(level, pos, targetBlock, player);
                    itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand((hand)));
                    return InteractionResultHolder.success(itemStack);
                }
            }
        }
        return InteractionResultHolder.pass(itemStack);
    }

    private void highlightNearbyOres(Level level, BlockPos center, Block targetBlock, Player player) {
        long expirationTime = System.currentTimeMillis() + HIGHLIGHT_DURATION_MS;
        lastHighlightTime = System.currentTimeMillis();
        // Iterate through blocks in a cube around the center position
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-HIGHLIGHT_RADIUS, -HIGHLIGHT_RADIUS, -HIGHLIGHT_RADIUS),
                center.offset(HIGHLIGHT_RADIUS, HIGHLIGHT_RADIUS, HIGHLIGHT_RADIUS))) {
            // If the block is the same as the target ore, add it to highlighted blocks
            if (level.getBlockState(pos).is(targetBlock)) {
                highlightedBlocks.put(pos.immutable(), expirationTime);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Only render after translucent blocks
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        PoseStack poseStack = event.getPoseStack();
        long currentTime = System.currentTimeMillis();

        // Remove expired highlighted blocks
        highlightedBlocks.entrySet().removeIf(entry -> currentTime > entry.getValue());

        if (!highlightedBlocks.isEmpty()) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            VertexConsumer builder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(OVERLAY_LINES);

            // Render outline for each highlighted block
            Vec3 cameraPos = event.getCamera().getPosition();
            for (BlockPos pos : highlightedBlocks.keySet()) {
                renderOutline(poseStack, pos, cameraPos, builder);
            }
        }

        // Only re-enable depth test if the highlight duration is over
        if (currentTime > lastHighlightTime + HIGHLIGHT_DURATION_MS) {
            RenderSystem.enableDepthTest();
        }
    }

    private static void renderOutline(PoseStack poseStack, BlockPos pos, Vec3 cameraPos, VertexConsumer builder) {
        poseStack.pushPose();
        poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

        // Define outline dimensions
        float minX = -0.002f, minY = -0.002f, minZ = -0.002f;
        float maxX = 1.002f, maxY = 1.002f, maxZ = 1.002f;

        // Helper method to draw a line
        BiConsumer<Vector3f, Vector3f> drawLine = (start, end) -> {
            builder.addVertex(poseStack.last().pose(), start.x(), start.y(), start.z()).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
            builder.addVertex(poseStack.last().pose(), end.x(), end.y(), end.z()).setColor(255, 255, 255, 255).setNormal(0, 1, 0);
        };

        // Bottom face
        drawLine.accept(new Vector3f(minX, minY, minZ), new Vector3f(maxX, minY, minZ));
        drawLine.accept(new Vector3f(maxX, minY, minZ), new Vector3f(maxX, minY, maxZ));
        drawLine.accept(new Vector3f(maxX, minY, maxZ), new Vector3f(minX, minY, maxZ));
        drawLine.accept(new Vector3f(minX, minY, maxZ), new Vector3f(minX, minY, minZ));

        // Top face
        drawLine.accept(new Vector3f(minX, maxY, minZ), new Vector3f(maxX, maxY, minZ));
        drawLine.accept(new Vector3f(maxX, maxY, minZ), new Vector3f(maxX, maxY, maxZ));
        drawLine.accept(new Vector3f(maxX, maxY, maxZ), new Vector3f(minX, maxY, maxZ));
        drawLine.accept(new Vector3f(minX, maxY, maxZ), new Vector3f(minX, maxY, minZ));

        // Vertical edges
        drawLine.accept(new Vector3f(minX, minY, minZ), new Vector3f(minX, maxY, minZ));
        drawLine.accept(new Vector3f(maxX, minY, minZ), new Vector3f(maxX, maxY, minZ));
        drawLine.accept(new Vector3f(maxX, minY, maxZ), new Vector3f(maxX, maxY, maxZ));
        drawLine.accept(new Vector3f(minX, minY, maxZ), new Vector3f(minX, maxY, maxZ));

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(RenderType.lines());

        poseStack.popPose();
    }

    // Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.orelocator_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

}