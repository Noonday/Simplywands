package net.bennysmith.simplywands.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.*;

public class ResurrectionWand extends Item {
    private static final int HISTORY_LENGTH = 400; // 20 seconds at 20 ticks per second
    private static final Map<UUID, ArrayDeque<PositionEntry>> positionHistory = new HashMap<>();

    public ResurrectionWand(Properties properties) {
        super(properties);
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player && !level.isClientSide) {
            UUID playerId = player.getUUID();
            ArrayDeque<PositionEntry> history = positionHistory.computeIfAbsent(playerId, k -> new ArrayDeque<>());
            history.addLast(new PositionEntry(player.position(), System.currentTimeMillis()));
            if (history.size() > HISTORY_LENGTH) {
                history.removeFirst();
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            if (player.getInventory().hasAnyOf(Set.of(this))) {
                event.setCanceled(true);
                UUID playerId = player.getUUID();
                ArrayDeque<PositionEntry> history = positionHistory.get(playerId);
                if (history != null && !history.isEmpty()) {
                    Vec3 pastPosition = findSafePosition(player.level(), history);
                    if (pastPosition != null) {
                        player.teleportTo(pastPosition.x, pastPosition.y, pastPosition.z);
                        player.setHealth(player.getMaxHealth());
                        player.setAirSupply(player.getMaxAirSupply());
                        player.fallDistance = 0.0F; // Reset fall distance
                        player.clearFire(); // Extinguish fire
                        // Remove negative potion effects
                        for (MobEffectInstance effect : new ArrayList<>(player.getActiveEffects())) {
                            if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                                player.removeEffect(effect.getEffect());
                            }
                        }
                        // Remove the wand from inventory
                        player.getInventory().clearOrCountMatchingItems(p -> p.getItem() == this, 1, player.inventoryMenu.getCraftSlots());
                        
                        // Play totem sound
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                                                 SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 
                                                 1.0F, 1.0F);
                        
                        // Spawn totem particles
                        if (player.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, 
                                                      player.getX(), player.getY() + 1.0D, player.getZ(), 
                                                      64, 0.0D, 0.0D, 0.0D, 0.5D);
                        }
                    }
                }
            }
        }
    }

    private Vec3 findSafePosition(Level level, ArrayDeque<PositionEntry> history) {
        long currentTime = System.currentTimeMillis();
        Vec3 lastSafePosition = null;
        long lastSafeTime = 0;

        for (PositionEntry entry : history) {
            Vec3 pos = entry.position();
            if (pos.y > -64) { // Check if the position is above the void
                BlockPos floorPos = new BlockPos((int)pos.x, (int)pos.y - 1, (int)pos.z);
                BlockPos playerPos = new BlockPos((int)pos.x, (int)pos.y, (int)pos.z);
                BlockPos abovePlayerPos = playerPos.above();
                
                if (level.getBlockState(floorPos).isSolidRender(level, floorPos) && 
                    !isHazardousBlock(level, playerPos) && 
                    !isHazardousBlock(level, abovePlayerPos)) {
                    lastSafePosition = pos;
                    lastSafeTime = entry.timestamp();
                    
                    // If this safe position is within the last 3 seconds, return it immediately
                    if (currentTime - lastSafeTime <= 3000) {
                        return lastSafePosition;
                    }
                }
            }
        }

        return lastSafePosition; // Return the last safe position found, or null if none found
    }

    private boolean isHazardousBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.MAGMA_BLOCK);
    }

    private static class PositionEntry {
        private final Vec3 position;
        private final long timestamp;

        public PositionEntry(Vec3 position, long timestamp) {
            this.position = position;
            this.timestamp = timestamp;
        }

        public Vec3 position() { return position; }
        public long timestamp() { return timestamp; }
    }

    // Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.resurrection_wand.tooltip"));
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
