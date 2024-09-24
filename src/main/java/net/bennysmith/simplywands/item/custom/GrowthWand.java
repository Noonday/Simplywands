package net.bennysmith.simplywands.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;
import java.util.Random;

import net.bennysmith.simplywands.Config;

public class GrowthWand extends Item {
    public GrowthWand(Properties properties) {
        super(properties);
    }

    //  Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.growth_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    //  Growth Function
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos blockPos = context.getClickedPos();
        InteractionHand hand = context.getHand();
        ItemStack itemStack = context.getItemInHand();

        if (!level.isClientSide && player != null) {
            boolean didApplyGrowth = false;
            Random random = new Random();

            // Use the configured range
            int range = Config.growthWandRange;
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos currentPos = blockPos.offset(x, 0, z);
                    BlockState blockState = level.getBlockState(currentPos);

                    // Check if the block is a crop
                    if (blockState.getBlock() instanceof CropBlock) {
                        CropBlock cropBlock = (CropBlock) blockState.getBlock();
                        IntegerProperty ageProperty = (IntegerProperty) cropBlock.getStateDefinition().getProperty("age");

                        if (ageProperty != null) {
                            int currentAge = blockState.getValue(ageProperty);
                            int maxAge = cropBlock.getMaxAge();

                            if (currentAge < maxAge) {
                                // Apply a random amount of growth
                                int growthAmount = 1 + random.nextInt(maxAge - currentAge + 1); // Random from 1 to (maxAge - currentAge)
                                int newAge = Math.min(currentAge + growthAmount, maxAge);

                                level.setBlockAndUpdate(currentPos, blockState.setValue(ageProperty, newAge));
                                if(Config.growthWandDurability != 0) {
                                itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand)); }
                                didApplyGrowth = true;

                                // Spawn bonemeal particles at the crop position
                                if (level instanceof ServerLevel) {
                                    ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                            currentPos.getX() + 0.5, currentPos.getY() + 0.5, currentPos.getZ() + 0.5,
                                            5, 0.5, 0.5, 0.5, 0.1);
                                }
                            }
                        }
                    }
                }
            }

            if (didApplyGrowth) {
                // Play bonemeal growth sound once per use, not per crop
                level.playSound(null, blockPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

                // Return success to show the item use animation and potentially trigger other effects
                return InteractionResult.SUCCESS;
            } else {
                // Return pass if no growth was applied, so the item doesn't show as being used
                return InteractionResult.PASS;
            }
        }

        return InteractionResult.PASS;  // Pass the action if on the client side
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

    // Durability
    @Override
    public int getMaxDamage(ItemStack stack) {
        return Config.growthWandDurability;
    }
}
