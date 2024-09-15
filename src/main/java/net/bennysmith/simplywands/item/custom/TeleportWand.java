package net.bennysmith.simplywands.item.custom;

import net.bennysmith.simplywands.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TeleportWand extends Item {
    public TeleportWand(Properties properties) {
        super(properties);
    }

    // Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.teleport_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // Handle right-click action
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            // Get the player's looking direction
            Vec3 lookVector = player.getLookAngle();

            // Calculate the target position
            Vec3 targetPos = player.position().add(lookVector.scale(Config.teleportWandDistance));
            BlockPos targetBlockPos = new BlockPos((int) targetPos.x, (int) targetPos.y, (int) targetPos.z);

            // Check if the target block is not air and is solid
            if (!level.getBlockState(targetBlockPos).isAir() && level.getBlockState(targetBlockPos).isSolid()) {
                // If the block is solid, cancel the teleport, spawn redstone particles, and return fail
                if (level instanceof ServerLevel) {
                    ((ServerLevel) level).sendParticles(ParticleTypes.SMOKE,
                            player.getX(), player.getY() + 1.0D, player.getZ(),
                            10, 0.5, 0.5, 0.5, 0.0);
                }
                // Set a cooldown of 0.5 seconds (10 ticks) after a successful teleport
                player.getCooldowns().addCooldown(this, 10);
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            } else {
                // Teleport the player
                player.teleportTo(targetPos.x, targetPos.y+1, targetPos.z);

                // Play ender pearl teleport sound at the new position
                level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

                // Spawn ender pearl particles (PORTAL particles) around the player at the new location
                if (level instanceof ServerLevel) {
                    ((ServerLevel) level).sendParticles(ParticleTypes.PORTAL,
                            targetPos.x + 0.5, targetPos.y + 0.5, targetPos.z + 0.5,
                            5, 0.5, 0.5, 0.5, 0.1);
                }

                // Swing the player's hand
                player.swing(hand);

                // Use some durability
                ItemStack itemStack = player.getItemInHand(hand);
                itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));;

                // Set a cooldown of 1.5 seconds (30 ticks) after a successful teleport
                player.getCooldowns().addCooldown(this, 30);

                // Return success to indicate that the action was successful
                return InteractionResultHolder.success(itemStack);
            }
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));  // Pass the action if on the client side
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
