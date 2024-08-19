package net.bennysmith.simplywands.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.core.particles.ParticleTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccelerationWand extends Item {
    private static final int TICK_MULTIPLIER = 64;
    private static final int DURATION = 20; // 1 second (20 ticks)

    private final Map<BlockPos, Integer> acceleratedBlocks = new HashMap<>();

    public AccelerationWand(Properties properties) {
        super(properties);
    }

    // Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.acceleration_wand1.tooltip"));
        tooltipComponents.add(Component.translatable("tooltip.simplywands.acceleration_wand2.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // Handle right-click action
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        BlockState blockState = level.getBlockState(pos);

        if (!level.isClientSide() && player != null) {
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel) level;

                // Check if the block should be accelerated
                if (shouldAccelerateBlock(blockState, serverLevel, pos)) {
                    // Accelerate the block
                    accelerateBlock(serverLevel, pos);

                    // Use some durability
                    ItemStack itemStack = player.getItemInHand(hand);
                    itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

                    // Set a cooldown of 1.5 seconds (30 ticks) after applying the acceleration
                    player.getCooldowns().addCooldown(this, 30);

                    // Spawn electric spark particles at the block position
                    serverLevel.sendParticles(ParticleTypes.WHITE_ASH,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            20, 0.5, 0.5, 0.5, 0.01);

                    level.playSound(null, pos, SoundEvents.BREEZE_SHOOT, SoundSource.BLOCKS, 1.0F, 1.0F);

                    // Return success
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // If the target doesn't need acceleration, return pass
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            // If the player is sneaking (shift-clicking), handle the action as a useOn
            BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                InteractionResult result = useOn(new UseOnContext(player, hand, hitResult));
                return new InteractionResultHolder<>(result, player.getItemInHand(hand));
            }
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private boolean shouldAccelerateBlock(BlockState blockState, ServerLevel level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);

        // Check if the block entity is one that benefits from acceleration
        return blockEntity instanceof FurnaceBlockEntity ||
                blockEntity instanceof BlastFurnaceBlockEntity ||
                blockEntity instanceof SmokerBlockEntity ||
                blockEntity instanceof HopperBlockEntity ||
                blockEntity instanceof SpawnerBlockEntity ||
                blockEntity instanceof BrewingStandBlockEntity ||
                blockState.is(Blocks.FARMLAND) ||
                blockState.is(BlockTags.CROPS);
    }

    private void accelerateBlock(ServerLevel level, BlockPos blockPos) {
        // If the block is already accelerated, refresh its duration
        if (acceleratedBlocks.containsKey(blockPos)) {
            acceleratedBlocks.put(blockPos, DURATION);
        } else {
            acceleratedBlocks.put(blockPos, DURATION);
            // Start the tick loop
            level.getServer().execute(() -> applyExtraTicks(level, blockPos));
        }
    }

    private void applyExtraTicks(ServerLevel level, BlockPos blockPos) {
        // Continue accelerating the block until the duration runs out
        int remainingTime = acceleratedBlocks.getOrDefault(blockPos, 0);

        if (remainingTime > 0) {
            BlockState blockState = level.getBlockState(blockPos);
            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            // Apply extra ticks based on the block type
            if (blockEntity instanceof FurnaceBlockEntity) {
                FurnaceBlockEntity furnace = (FurnaceBlockEntity) blockEntity;
                for (int i = 0; i < TICK_MULTIPLIER; i++) {
                    FurnaceBlockEntity.serverTick(level, blockPos, blockState, furnace);
                }
            } else if (blockEntity instanceof BlastFurnaceBlockEntity) {
                BlastFurnaceBlockEntity blastFurnace = (BlastFurnaceBlockEntity) blockEntity;
                for (int i = 0; i < TICK_MULTIPLIER; i++) {
                    BlastFurnaceBlockEntity.serverTick(level, blockPos, blockState, blastFurnace);
                }
            } else if (blockEntity instanceof BrewingStandBlockEntity) {
                BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) blockEntity;
                for (int i = 0; i < TICK_MULTIPLIER; i++) {
                    BrewingStandBlockEntity.serverTick(level, blockPos, blockState, brewingStand);
                }
            } else if (blockEntity instanceof SmokerBlockEntity) {
                SmokerBlockEntity smoker = (SmokerBlockEntity) blockEntity;
                for (int i = 0; i < TICK_MULTIPLIER; i++) {
                    SmokerBlockEntity.serverTick(level, blockPos, blockState, smoker);
                }
            } else if (blockEntity instanceof HopperBlockEntity) {
                HopperBlockEntity hopper = (HopperBlockEntity) blockEntity;
                for (int i = 0; i < TICK_MULTIPLIER; i++) {
                    hopper.pushItemsTick(level, blockPos, blockState, hopper);
                }
            } else if (blockEntity instanceof SpawnerBlockEntity) {
                SpawnerBlockEntity spawner = (SpawnerBlockEntity) blockEntity;
                for (int i = 0; i < TICK_MULTIPLIER; i++) {
                    spawner.getSpawner().serverTick(level, blockPos);
                }
            } else if (blockState.is(Blocks.FARMLAND) || blockState.is(BlockTags.CROPS)) {
                // Apply extra random ticks for farmland and crops
                for (int i = 0; i < TICK_MULTIPLIER; i++) {
                    blockState.randomTick(level, blockPos, level.random);
                }
            }

            // Update remaining time
            acceleratedBlocks.put(blockPos, remainingTime - 1);

            // Schedule the next tick application (next game tick)
            level.getServer().execute(() -> applyExtraTicks(level, blockPos));
        } else {
            // Remove the block from the accelerated list
            acceleratedBlocks.remove(blockPos);
        }
    }
}
