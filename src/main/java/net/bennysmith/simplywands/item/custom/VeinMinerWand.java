package net.bennysmith.simplywands.item.custom;

import net.bennysmith.simplywands.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VeinMinerWand extends Item {
    public VeinMinerWand(Properties properties) {
        super(properties);
    }

    // Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.vein_miner_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Block targetBlock = world.getBlockState(pos).getBlock();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();

        Set<BlockPos> vein = findVein(world, pos, targetBlock, new HashSet<>());
        if (targetBlock.defaultBlockState().is(Tags.Blocks.ORES)) {
            for (BlockPos blockPos : vein) {
                world.destroyBlock(blockPos, true);
            }
            ItemStack itemStack = player.getItemInHand(hand);
            itemStack.hurtAndBreak(vein.size(), player, LivingEntity.getSlotForHand(hand));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private Set<BlockPos> findVein(Level world, BlockPos pos, Block targetBlock, Set<BlockPos> vein) {
        if (vein.size() >= Config.veinMinerMaxBlocks || !world.getBlockState(pos).is(targetBlock)) {
            return vein;
        }

        vein.add(pos);

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // Skip the center block
                    BlockPos newPos = pos.offset(x, y, z);
                    if (!vein.contains(newPos)) {
                        findVein(world, newPos, targetBlock, vein);
                    }
                }
            }
        }

        return vein;
    }

    // Disallow enchanting
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }
}
