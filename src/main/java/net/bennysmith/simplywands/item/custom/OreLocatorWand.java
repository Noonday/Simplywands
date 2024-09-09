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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;
import java.util.Queue;

public class OreLocatorWand extends Item {

    private static final ConcurrentHashMap<BlockPos, BlockHighlight> highlightedBlocks = new ConcurrentHashMap<>();
    private static long lastHighlightTime = 0;

    public static class BlockHighlight {
        public final Block block;
        public final long expirationTime;

        public BlockHighlight(Block block, long expirationTime) {
            this.block = block;
            this.expirationTime = expirationTime;
        }
    }

    public OreLocatorWand(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        HitResult hitResult = player.pick(5.0D, 0.0F, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos pos = blockHitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            Block targetBlock = state.getBlock();

            if (targetBlock.defaultBlockState().is(Tags.Blocks.ORES)) {
                if (!level.isClientSide()) {
                    if (level.getServer() != null && !level.getServer().isSingleplayer()) {
                        // We're on a dedicated server
                        displayNearestOre(level, pos, targetBlock, player);
                    } else {
                        // We're on an integrated server (singleplayer or LAN)
                        highlightNearbyOres(level, pos, targetBlock, player);
                    }
                    itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand((hand)));
                }
                return InteractionResultHolder.success(itemStack);
            }
        }
        return InteractionResultHolder.pass(itemStack);
    }

    private void displayNearestOre(Level level, BlockPos center, Block targetBlock, Player player) {
        Set<BlockPos> connectedOres = findConnectedOres(level, center, targetBlock);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        BlockPos nearestOre = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (int x = -Config.highlightRadius; x <= Config.highlightRadius; x++) {
            for (int y = -Config.highlightRadius; y <= Config.highlightRadius; y++) {
                for (int z = -Config.highlightRadius; z <= Config.highlightRadius; z++) {
                    mutablePos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState state = level.getBlockState(mutablePos);
                    if (state.is(targetBlock) && !connectedOres.contains(mutablePos)) {
                        double distance = center.distSqr(mutablePos);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestOre = mutablePos.immutable();
                        }
                    }
                }
            }
        }

        if (nearestOre != null) {
            String oreName = targetBlock.getName().getString();
            String message = String.format("Nearest separate %s vein found at: X: %d, Y: %d, Z: %d", 
                                           oreName, nearestOre.getX(), nearestOre.getY(), nearestOre.getZ());
            player.sendSystemMessage(Component.literal(message));
        } else {
            player.sendSystemMessage(Component.literal("No nearby separate ore vein of the same type found."));
        }
    }

    private Set<BlockPos> findConnectedOres(Level level, BlockPos start, Block targetBlock) {
        Set<BlockPos> connectedOres = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        toCheck.add(start);

        while (!toCheck.isEmpty()) {
            BlockPos current = toCheck.poll();
            if (connectedOres.contains(current)) continue;

            BlockState state = level.getBlockState(current);
            if (state.is(targetBlock)) {
                connectedOres.add(current);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            BlockPos neighbor = current.offset(dx, dy, dz);
                            if (!connectedOres.contains(neighbor)) {
                                toCheck.add(neighbor);
                            }
                        }
                    }
                }
            }
        }

        return connectedOres;
    }

    private boolean isAdjacentToCenter(int x, int y, int z) {
        return Math.abs(x) <= 1 && Math.abs(y) <= 1 && Math.abs(z) <= 1;
    }

    private void highlightNearbyOres(Level level, BlockPos center, Block targetBlock, Player player) {
        long expirationTime = System.currentTimeMillis() + Config.highlightDurationMs;
        lastHighlightTime = System.currentTimeMillis();
        
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = -Config.highlightRadius; x <= Config.highlightRadius; x++) {
            for (int y = -Config.highlightRadius; y <= Config.highlightRadius; y++) {
                for (int z = -Config.highlightRadius; z <= Config.highlightRadius; z++) {
                    mutablePos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState state = level.getBlockState(mutablePos);
                    if (state.is(targetBlock)) {
                        highlightedBlocks.put(mutablePos.immutable(), new BlockHighlight(state.getBlock(), expirationTime));
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.orelocator_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static ConcurrentHashMap<BlockPos, BlockHighlight> getHighlightedBlocks() {
        return highlightedBlocks;
    }

    public static long getLastHighlightTime() {
        return lastHighlightTime;
    }

    public static void addHighlightedBlock(BlockPos pos, Block block, long expirationTime) {
        highlightedBlocks.put(pos, new BlockHighlight(block, expirationTime));
    }

    public static void removeHighlightedBlock(BlockPos pos) {
        highlightedBlocks.remove(pos);
    }
}