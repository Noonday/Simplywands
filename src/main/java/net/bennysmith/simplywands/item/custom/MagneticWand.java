package net.bennysmith.simplywands.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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

public class MagneticWand extends Item {
    // Indicates whether the wand is active
    private boolean isActive = false;

    public MagneticWand(Properties properties) {
        super(properties);
        NeoForge.EVENT_BUS.register(this);
    }

    // Determines if the item has the "foil" effect (glowing) based on its active state
    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive;
    }

    // Called when the player uses the wand (right-clicks)
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {    // Ensure this only runs on the server side
            isActive = !isActive;       // Toggle the active state
            BlockPos blockPos = player.blockPosition();

            // Play a sound based on whether the wand is activated or deactivated
            if (isActive) {
                level.playSound(null, blockPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.8F, 1.0F); // Normal pitch when activated
                player.displayClientMessage(Component.literal("Magnet on").withStyle(ChatFormatting.GOLD), true); // Display "Magnet on" in golden color
            } else {
                level.playSound(null, blockPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.8F, 0.5F); // Lower pitch when deactivated
                player.displayClientMessage(Component.literal("Magnet off").withStyle(ChatFormatting.GOLD), true); // Display "Magnet off" in golden color
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }


    // Event handler: Called every tick for each player
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Check if the player has the wand in their inventory
        boolean hasMagneticWand = player.getInventory().contains(new ItemStack(this));

        // Attract items if the wand is active and present in the player's inventory
        if (isActive && hasMagneticWand) {
            attractItems(player);
        }
    }


    // Handles the attraction of nearby items towards the player
    private void attractItems(Player player) {
        Level level = player.level();
        // Find all items within a 5-block radius around the player
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class,
                player.getBoundingBox().inflate(5.0));

        // Move each item towards the player
        for (ItemEntity item : items) {
            double dx = player.getX() - item.getX();
            double dy = player.getY() - item.getY();
            double dz = player.getZ() - item.getZ();

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);   // Calculate distance
            double speed = 0.5;                                         // Set the speed of attraction

            // Set the item's movement vector towards the player
            item.setDeltaMovement(dx / distance * speed, dy / distance * speed, dz / distance * speed);
        }
    }

    // Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.magnetic_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
