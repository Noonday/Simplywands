package net.bennysmith.simplywands.item.custom;

import net.bennysmith.simplywands.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class LoveWand extends Item {
    public LoveWand(Properties properties) {
        super(properties);
    }

    //  Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.love_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // Method for breeding entities
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof Animal animal) {
            if (!player.level().isClientSide && animal.getAge() == 0 && animal.canFallInLove()) {
                animal.setInLove(player);

                // Use some durability
                if (Config.loveWandDurability != 0) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand)); }

                // Spawn heart particles
                player.level().addParticle(ParticleTypes.HEART, animal.getX(), animal.getY() + 0.5, animal.getZ(), 0, 0, 0);

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    // Method for luring
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (isSelected && entity instanceof Player player && !level.isClientSide) {
            double lureRange = Config.loveWandLureRange;
            AABB searchArea = player.getBoundingBox().inflate(lureRange);
            List<Animal> nearbyAnimals = level.getEntitiesOfClass(Animal.class, searchArea,
                    animal -> animal.getAge() == 0 && animal.canFallInLove());

            for (Animal animal : nearbyAnimals) {
                double distance = animal.distanceToSqr(player);
                if (distance > 4.0 && distance < lureRange * lureRange) {
                    animal.getLookControl().setLookAt(player, 10.0F, animal.getMaxHeadXRot());
                    animal.getNavigation().moveTo(player, 1.0);
                }
            }
        }
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
        return Config.loveWandDurability;
    }
}

