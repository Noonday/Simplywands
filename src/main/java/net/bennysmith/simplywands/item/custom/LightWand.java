package net.bennysmith.simplywands.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LightWand extends Item {
    private static final int DEFAULT_LIGHT_LEVEL = 15;
    private int currentLightLevel = DEFAULT_LIGHT_LEVEL;

    public LightWand(Properties properties) {
        super(properties);
    }

    // Tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.simplywands.light_wand.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // Handle right-click action
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Perform a ray trace to find the block being targeted
            HitResult hitResult = player.pick(20.0D, 0.0F, false);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockPos placePos = blockPos.relative(blockHitResult.getDirection());

                // Get the block state at the target position
                BlockState blockState = level.getBlockState(placePos);

                // Check if the target block is within the player's reach range
                Vec3 playerEyePosition = player.getEyePosition(1.0F);
                Vec3 targetPosition = new Vec3(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
                double distance = playerEyePosition.distanceTo(targetPosition);

                if (distance <= 5.0) { // Normal block placement reach is 5 blocks
                    // Check if the target block is air or can be replaced
                    if (blockState.isAir() || blockState.canBeReplaced()) {
                        // Get the current light level from the item
                        int lightLevel = getLightLevel();
                        if (lightLevel == 0) {
                            lightLevel = DEFAULT_LIGHT_LEVEL;
                        }

                        // Set the block to a light source with the stored light level
                        level.setBlockAndUpdate(placePos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel));

                        // Use some durability
                        itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

                        // Return success
                        return InteractionResultHolder.success(itemStack);
                    }
                }
            }

            // If the target is invalid or out of reach, return fail
            return InteractionResultHolder.fail(itemStack);
        }

        // Pass the action if on the client side
        return InteractionResultHolder.pass(player.getItemInHand(hand));
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

    // Add this new method to get the current light level
    public int getLightLevel() {
        return currentLightLevel;
    }

    // Change this to public so it can be accessed from the client
    public void setLightLevel(int level) {
        currentLightLevel = Math.max(0, Math.min(15, level));
    }

    public void handleScroll(Player player, double scrollDelta) {
        if (player.isShiftKeyDown()) {
            int currentLevel = getLightLevel();
            int newLevel;
            if (scrollDelta > 0) {
                newLevel = Math.min(15, currentLevel + 1);
            } else {
                newLevel = Math.max(1, currentLevel - 1);
            }
            setLightLevel(newLevel);
            player.displayClientMessage(Component.literal("Light level set to: " + newLevel), true);
        }
    }

    // Modify this method to use the current light level
    public void handleScrollServer(Player player, int delta) {
        int newLevel = Math.max(1, Math.min(15, currentLightLevel + delta));
        setLightLevel(newLevel);
        player.displayClientMessage(Component.literal("Light level set to: " + newLevel), true);
    }
}
