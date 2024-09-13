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
import net.bennysmith.simplywands.network.HighlightOresPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.ArrayList;

public class OreLocatorWand extends Item {

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
                    List<BlockPos> orePositions = collectOrePositions(level, pos, targetBlock);

                    // Send payload to client
                    HighlightOresPayload payload = new HighlightOresPayload(orePositions);
                    PacketDistributor.sendToPlayer((ServerPlayer) player, payload);

                    itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand((hand)));
                }
                return InteractionResultHolder.success(itemStack);
            }
        }
        return InteractionResultHolder.pass(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.orelocator_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private List<BlockPos> collectOrePositions(Level level, BlockPos center, Block targetBlock) {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        int radius = Config.highlightRadius;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    mutablePos.setWithOffset(center, x, y, z);
                    if (level.isLoaded(mutablePos)) {
                        BlockState state = level.getBlockState(mutablePos);
                        if (state.is(targetBlock)) {
                            positions.add(mutablePos.immutable());
                        }
                    }
                }
            }
        }
        return positions;
    }
}