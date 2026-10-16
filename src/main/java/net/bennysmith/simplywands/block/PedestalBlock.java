package net.bennysmith.simplywands.block;

import com.mojang.serialization.MapCodec;
import net.bennysmith.simplywands.blockentity.PedestalBlockEntity;
import net.bennysmith.simplywands.simplywands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.bennysmith.simplywands.blockentity.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PedestalBlock extends BaseEntityBlock {
    public static final MapCodec<PedestalBlock> CODEC = simpleCodec(PedestalBlock::new);

    public PedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    //  BaseEntityBlock hides itself by default - without this the model never draws
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide || type != ModBlockEntities.PEDESTAL.get() ? null
                : (BlockEntityTicker<T>) (BlockEntityTicker<PedestalBlockEntity>) (lvl, blockPos, blockState, entity) -> entity.tickServer();
    }

    //  Right click with an empty hand to take the displayed item back out
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)) {
            return InteractionResult.PASS;
        }
        if (pedestal.getWand().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            //  Swing only - the server does the actual inventory work
            return InteractionResult.SUCCESS;
        }
        if (!player.getInventory().add(pedestal.getWand())) {
            return InteractionResult.FAIL;
        }
        for (ItemStack stored : pedestal.drainStoredItems()) {
            popResource(level, pos, stored);
        }
        pedestal.clearWand();
        return InteractionResult.CONSUME;
    }

    //  Right click with an item to display it
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                             Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty() || !isWand(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal) || !pedestal.getWand().isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            ItemStack displayed = stack.copy();
            displayed.setCount(1);
            pedestal.setWand(displayed);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    //  The wands have no shared base class or tag, so match on the mod namespace.
    //  BlockItem is excluded so a pedestal can't be placed inside a pedestal.
    private static boolean isWand(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null
                && id.getNamespace().equals(simplywands.MOD_ID)
                && !(stack.getItem() instanceof BlockItem);
    }

    //  Don't lose the displayed item when the pedestal is broken
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal) {
                if (!level.isClientSide()) {
                    if (!pedestal.getWand().isEmpty()) popResource(level, pos, pedestal.getWand());
                    for (ItemStack stored : pedestal.drainStoredItems()) popResource(level, pos, stored);
                }
                pedestal.clearWand();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}