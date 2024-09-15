package net.bennysmith.simplywands.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

public class FoodWand extends Item {
    private boolean isActive = false;

    public FoodWand(Properties properties) {
        super(properties);
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            isActive = !isActive;
            if (isActive) {
                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
                player.displayClientMessage(Component.literal("Food Wand activated").withStyle(ChatFormatting.GREEN), true);
            } else {
                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 0.5F);
                player.displayClientMessage(Component.literal("Food Wand deactivated").withStyle(ChatFormatting.RED), true);
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        
        if (!player.level().isClientSide() && isActive) {
            boolean hasFoodWand = player.getInventory().contains(new ItemStack(this));
            
            if (hasFoodWand && player.getFoodData().needsFood()) {
                for (ItemStack itemStack : player.getInventory().items) {
                    FoodProperties foodProperties = itemStack.get(DataComponents.FOOD);
                    if (foodProperties != null) {
                        player.eat(player.level(), itemStack);
                        break;
                    }
                }
            }
        }
    }

    // Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.food_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
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
