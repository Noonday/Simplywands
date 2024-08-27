package net.bennysmith.simplywands.item.custom;

import net.bennysmith.simplywands.Config;
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
import net.neoforged.neoforge.common.Tags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OreLocatorWand extends Item {

    private static final Map<BlockPos, Long> highlightedBlocks = new HashMap<>();
    private static long lastHighlightTime = 0;

    public OreLocatorWand(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            HitResult hitResult = player.pick(5.0D, 0.0F, false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos pos = blockHitResult.getBlockPos();
                BlockState state = level.getBlockState(pos);
                Block targetBlock = state.getBlock();

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
        long expirationTime = System.currentTimeMillis() + Config.highlightDurationMs;
        lastHighlightTime = System.currentTimeMillis();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-Config.highlightRadius, -Config.highlightRadius, -Config.highlightRadius),
                center.offset(Config.highlightRadius, Config.highlightRadius, Config.highlightRadius))) {
            if (level.getBlockState(pos).is(targetBlock)) {
                highlightedBlocks.put(pos.immutable(), expirationTime);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.orelocator_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static Map<BlockPos, Long> getHighlightedBlocks() {
        return highlightedBlocks;
    }

    public static long getLastHighlightTime() {
        return lastHighlightTime;
    }
}