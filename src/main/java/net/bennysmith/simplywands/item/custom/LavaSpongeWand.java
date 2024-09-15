package net.bennysmith.simplywands.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

import net.bennysmith.simplywands.Config;

public class LavaSpongeWand extends Item {
    public LavaSpongeWand(Properties properties) {
        super(properties);
    }

    //  Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.lavasponge_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    //  Lava removing function
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            BlockPos playerBBBlockPos = new BlockPos((int) player.position().x(),
                    (int) player.position().y(),
                    (int) player.position().z());

            // Get the item stack being used
            ItemStack itemStack = player.getItemInHand(hand);

            boolean didRemoveLava = false;  // Track if any lava was removed

            int range = Config.lavaSpongeWandRange;
            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos blockPos = playerBBBlockPos.offset(x, y, z);
                        BlockState blockState = level.getBlockState(blockPos);

                        if (blockState.getFluidState().is(Fluids.LAVA)) {
                            level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                            itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                            didRemoveLava = true;  // Set to true if any lava is removed

                            // Play the sound for picking up lava
                            level.playSound(null, blockPos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                    }
                }
            }

            if (didRemoveLava) {
                // Return success to swing the item only if lava was removed
                return InteractionResultHolder.success(itemStack);
            } else {
                // Return pass to prevent swinging the item if no lava was removed
                return InteractionResultHolder.pass(itemStack);
            }
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));  // Pass the action if on the client side
    }


}
