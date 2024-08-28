package net.bennysmith.simplywands.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;

import java.util.List;

public class FoodWand extends Item {
    private int tickCounter = 0;
    private boolean isActive = false;

    public FoodWand(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            isActive = !isActive;
            BlockPos blockPos = player.blockPosition();

            if (isActive) {
                level.playSound(null, blockPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.8F, 1.0F);
                player.displayClientMessage(Component.literal("Food Wand activated").withStyle(ChatFormatting.GREEN), true);
            } else {
                level.playSound(null, blockPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.8F, 0.5F);
                player.displayClientMessage(Component.literal("Food Wand deactivated").withStyle(ChatFormatting.RED), true);
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (isActive && entity instanceof Player player) {
            tickCounter++;

            if (tickCounter >= 20) {
                tickCounter = 0;

                int currentFoodLevel = player.getFoodData().getFoodLevel();

                if (currentFoodLevel < 20) {
                    ItemStack foodStack = findFoodInInventory(player);
                    if (!foodStack.isEmpty() && foodStack.has(DataComponents.FOOD)) {
                        float currentSaturation = player.getFoodData().getSaturationLevel();

                        int foodValue = foodStack.getFoodProperties(null).nutrition();
                        float saturationValue = foodStack.getFoodProperties(null).saturation();

                        int newFoodLevel = Math.min(currentFoodLevel + foodValue, 20);
                        float newSaturation = currentSaturation + saturationValue;

                        player.getFoodData().setFoodLevel(newFoodLevel);
                        player.getFoodData().setSaturation(newSaturation);

                        if (!world.isClientSide()) {
                            BlockPos blockPos = player.blockPosition();
                            world.playSound(null, blockPos, SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0F, 1.0F);
                        }

                        foodStack.shrink(1);
                    }
                }
            }
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    private ItemStack findFoodInInventory(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack.has(DataComponents.FOOD)) {
                return itemStack;
            }
        }
        return ItemStack.EMPTY;
    }

    // Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.food_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
