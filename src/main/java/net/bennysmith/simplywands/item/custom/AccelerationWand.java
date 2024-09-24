package net.bennysmith.simplywands.item.custom;

import net.bennysmith.simplywands.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bennysmith.simplywands.Config;

public class AccelerationWand extends Item {
    private static final int DURATION = 20; // 1 second (20 ticks)

    private final Map<BlockPos, Integer> acceleratedBlocks = new HashMap<>();

    public AccelerationWand(Properties properties) {
        super(properties);
    }

    // Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.acceleration_wand1.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private boolean shouldAccelerateBlock(BlockState blockState, ServerLevel level, BlockPos pos) {
        // First, check if the block is in the blacklist
        if (blockState.is(ModTags.Blocks.NON_ACCELERATABLE_BLOCKS)) {
            return false;
        }

        // Then check if it's in the whitelist
        if (blockState.is(ModTags.Blocks.ACCELERATABLE_BLOCKS)) {
            return true;
        }

        // Check for ticker or random ticks
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) blockState.getTicker(level, blockEntity.getType());
            return ticker != null;
        }

        return blockState.isRandomlyTicking();
    }


    // Method called when the wand is used on a block
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        BlockState blockState = level.getBlockState(pos);

        if (!level.isClientSide() && player != null && level instanceof ServerLevel serverLevel) {
            if (shouldAccelerateBlock(blockState, serverLevel, pos)) {
                // Accelerate the block, damage the wand, add cooldown, and spawn effects
                accelerateBlock(serverLevel, pos);
                ItemStack itemStack = player.getItemInHand(hand);
                if(Config.accelerationWandDurability != 0) {
                itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand)); }
                player.getCooldowns().addCooldown(this, 30);
                spawnParticlesAndPlaySound(serverLevel, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }


    // Method called when the wand is used in the air
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            // Get the block the player is looking at
            BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                // Use the wand on the looked-at block
                InteractionResult result = useOn(new UseOnContext(player, hand, hitResult));
                return new InteractionResultHolder<>(result, player.getItemInHand(hand));
            }
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    // Method to start the acceleration process for a block
    private void accelerateBlock(ServerLevel level, BlockPos blockPos) {
        acceleratedBlocks.put(blockPos, DURATION);
        level.getServer().execute(() -> applyExtraTicks(level, blockPos));
    }

    // Method to apply extra ticks to an accelerated block
    private void applyExtraTicks(ServerLevel level, BlockPos blockPos) {
        int remainingTime = acceleratedBlocks.getOrDefault(blockPos, 0);
        if (remainingTime > 0) {
            BlockState blockState = level.getBlockState(blockPos);
            if (shouldAccelerateBlock(blockState, level, blockPos)) {
                BlockEntity blockEntity = level.getBlockEntity(blockPos);
                if (blockEntity != null) {
                    BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) blockState.getTicker(level, blockEntity.getType());
                    if (ticker != null) {
                        // Apply extra ticks to the block entity
                        for (int i = 0; i < Config.accelerationTickMultiplier; i++) {
                            ticker.tick(level, blockPos, blockState, blockEntity);
                        }
                    } else {
                        // Fallback for blocks that don't have a ticker
                        blockEntity.setChanged();
                    }
                }

                // Apply random tick for blocks that use it (like crops)
                if (blockState.isRandomlyTicking()) {
                    for (int i = 0; i < Config.accelerationTickMultiplier; i++) {
                        blockState.randomTick(level, blockPos, level.random);
                    }
                }

                // Trigger block update
                level.neighborChanged(blockPos, blockState.getBlock(), blockPos);

                acceleratedBlocks.put(blockPos, remainingTime - 1);
                level.getServer().execute(() -> applyExtraTicks(level, blockPos));
            } else {
                acceleratedBlocks.remove(blockPos);
            }
        } else {
            acceleratedBlocks.remove(blockPos);
        }
    }

    // Method to spawn particles and play sound when the wand is used
    private void spawnParticlesAndPlaySound(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.WHITE_ASH,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                20, 0.5, 0.5, 0.5, 0.01);
        level.playSound(null, pos, SoundEvents.BREEZE_SHOOT, SoundSource.BLOCKS, 1.0F, 1.0F);
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

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Config.accelerationWandDurability;
    }
}