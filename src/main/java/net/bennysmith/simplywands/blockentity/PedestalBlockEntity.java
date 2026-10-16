package net.bennysmith.simplywands.blockentity;

import net.bennysmith.simplywands.Config;
import net.bennysmith.simplywands.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PedestalBlockEntity extends BlockEntity {
    public static final String WAND_TAG = "Wand";
    private static final int ITEM_SLOTS = 27;
    private static final int FLUID_CAPACITY = 32 * 1000;

    private ItemStack wand = ItemStack.EMPTY;
    private final ItemStackHandler storedItems = new ItemStackHandler(ITEM_SLOTS) {
        @Override protected void onContentsChanged(int slot) { setChangedAndSync(); }
    };
    private final FluidTank fluidTank = new FluidTank(FLUID_CAPACITY, this::isAcceptedFluid) {
        @Override protected void onContentsChanged() { setChangedAndSync(); }
    };
    private final Set<BlockPos> placedLights = ConcurrentHashMap.newKeySet();
    private int tickCounter;

    public PedestalBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.PEDESTAL.get(), pos, state); }
    public ItemStack getWand() { return wand; }
    public ItemStackHandler getStoredItems() { return storedItems; }
    public IFluidHandler getFluidHandler() { return fluidTank; }

    public void setWand(ItemStack stack) {
        Item old = wand.getItem();
        if (!stack.isEmpty() && old != stack.getItem()) tickCounter = 0;
        if (old != ItemStack.EMPTY.getItem() && (stack.isEmpty() || old != stack.getItem())) onWandRemoved();
        wand = stack;
        setChangedAndSync();
    }

    public void clearWand() { setWand(ItemStack.EMPTY); }

    private boolean isAcceptedFluid(FluidStack stack) {
        return (!wand.isEmpty() && wand.is(ModItems.SPONGE_WAND.get()) && stack.is(Fluids.WATER))
                || (!wand.isEmpty() && wand.is(ModItems.LAVASPONGE_WAND.get()) && stack.is(Fluids.LAVA));
    }

    public void tickServer() {
        if (!(level instanceof ServerLevel server) || wand.isEmpty()) return;
        if (++tickCounter % Config.pedestalOperationInterval != 0) return;
        Item item = wand.getItem();
        if (item == ModItems.SPONGE_WAND.get() || item == ModItems.LAVASPONGE_WAND.get()) { collectFluid(server, item == ModItems.SPONGE_WAND.get() ? Fluids.WATER : Fluids.LAVA); transferFluid(server); }
        else if (item == ModItems.MAGNETIC_WAND.get()) { attractItems(server); transferItems(server); }
        else if (item == ModItems.GROWTH_WAND.get()) growCrops(server);
        else if (item == ModItems.ACCELERATION_WAND.get()) accelerateBelow(server);
        else if (item == ModItems.LOVE_WAND.get()) breedAnimals(server);
        else if (item == ModItems.FOOD_WAND.get()) feedPlayers(server);
        else if (item == ModItems.LIGHT_WAND.get()) placeLights(server);
        else if (item == ModItems.VEIN_MINER_WAND.get()) mineOres(server);
    }

    private void collectFluid(ServerLevel level, Fluid fluid) {
        if (fluidTank.getSpace() < 1000) return;
        for (BlockPos pos : List.of(worldPosition.below(), worldPosition.above())) {
            if (level.getFluidState(pos).is(fluid) && level.getFluidState(pos).isSource()) {
                FluidStack stack = new FluidStack(fluid, 1000);
                if (fluidTank.fill(stack, IFluidHandler.FluidAction.SIMULATE) == 1000) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    fluidTank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
                    level.playSound(null, pos, fluid == Fluids.WATER ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, .6F, 1F);
                }
                return;
            }
        }
    }

    private void transferFluid(ServerLevel level) {
        for (Direction side : Direction.values()) {
            IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(side), side.getOpposite());
            if (target == null || fluidTank.isEmpty()) continue;
            FluidStack offered = fluidTank.getFluid().copy();
            int moved = target.fill(offered, IFluidHandler.FluidAction.EXECUTE);
            if (moved > 0) fluidTank.drain(moved, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void attractItems(ServerLevel level) {
        Vec3 target = Vec3.atCenterOf(worldPosition).add(0, .8, 0);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition).inflate(Config.pedestalMagneticRange))) {
            if (entity.blockPosition().getY() < worldPosition.getY()) continue;
            Vec3 delta = target.subtract(entity.position());
            if (delta.lengthSqr() > .01) entity.setDeltaMovement(delta.normalize().scale(Config.pedestalMagnetSpeed));
            if (delta.lengthSqr() < 1) { ItemStack remainder = insertStored(entity.getItem()); entity.setItem(remainder); if (remainder.isEmpty()) entity.discard(); }
        }
    }

    private ItemStack insertStored(ItemStack stack) {
        ItemStack remainder = stack.copy();
        for (int i = 0; i < storedItems.getSlots() && !remainder.isEmpty(); i++) remainder = storedItems.insertItem(i, remainder, false);
        return remainder;
    }

    private void transferItems(ServerLevel level) {
        for (Direction side : Direction.values()) {
            net.neoforged.neoforge.items.IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK, worldPosition.relative(side), side.getOpposite());
            if (target == null) continue;
            for (int i = 0; i < storedItems.getSlots(); i++) {
                ItemStack remainder = storedItems.getStackInSlot(i).copy();
                for (int j = 0; j < target.getSlots() && !remainder.isEmpty(); j++) remainder = target.insertItem(j, remainder, false);
                storedItems.setStackInSlot(i, remainder);
            }
        }
    }

    private void growCrops(ServerLevel level) {
        int r = Config.pedestalGrowthRange;
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
            BlockPos pos = worldPosition.offset(x, 0, z); BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof CropBlock crop && crop.isValidBonemealTarget(level, pos, state)) crop.performBonemeal(level, level.random, pos, state);
        }
    }

    @SuppressWarnings("unchecked")
    private void accelerateBelow(ServerLevel level) {
        BlockPos pos = worldPosition.below(); BlockState state = level.getBlockState(pos);
        if (state.isRandomlyTicking()) for (int i = 0; i < Config.pedestalAccelerationMultiplier; i++) state.randomTick(level, pos, level.random);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity != null) { BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) state.getTicker(level, entity.getType()); if (ticker != null) for (int i = 0; i < Config.pedestalAccelerationMultiplier; i++) ticker.tick(level, pos, state, entity); }
    }

    private void breedAnimals(ServerLevel level) {
        List<Animal> animals = level.getEntitiesOfClass(Animal.class, new AABB(worldPosition).inflate(Config.pedestalLoveRange), a -> a.getAge() == 0 && a.canFallInLove());
        for (int i = 0; i + 1 < animals.size(); i += 2) { animals.get(i).setInLove(null); animals.get(i + 1).setInLove(null); }
    }

    private void feedPlayers(ServerLevel level) { for (Player player : level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(Config.pedestalFoodRange))) player.getFoodData().eat(Config.pedestalFoodAmount, (float) Config.pedestalSaturation); }

    private void placeLights(ServerLevel level) {
        if (!placedLights.isEmpty()) return;
        int r = Config.pedestalLightRadius, placed = 0;
        for (int x = -r; x <= r && placed < 128; x += 4) for (int y = -r; y <= r && placed < 128; y += 4) for (int z = -r; z <= r && placed < 128; z += 4) {
            if (x*x + y*y + z*z > r*r) continue; BlockPos pos = worldPosition.offset(x, y, z);
            if (level.hasChunkAt(pos) && level.getBlockState(pos).isAir()) { level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, Config.pedestalLightLevel), Block.UPDATE_CLIENTS); placedLights.add(pos.immutable()); placed++; }
        }
    }

    private void removePlacedLights(ServerLevel level) { for (BlockPos pos : placedLights) if (level.hasChunkAt(pos) && level.getBlockState(pos).is(Blocks.LIGHT)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL); placedLights.clear(); }
    private void mineOres(ServerLevel level) {
        int r = Config.pedestalVeinRange, max = Config.pedestalVeinMaxBlocks; List<BlockPos> ores = new ArrayList<>();
        for (int x = -r; x <= r && ores.size() < max; x++) for (int y = -r; y <= r && ores.size() < max; y++) for (int z = -r; z <= r && ores.size() < max; z++) if (x*x+y*y+z*z <= r*r) { BlockPos pos = worldPosition.offset(x,y,z); if (level.hasChunkAt(pos) && level.getBlockState(pos).is(net.neoforged.neoforge.common.Tags.Blocks.ORES)) ores.add(pos.immutable()); }
        for (BlockPos pos : ores) { for (ItemStack drop : Block.getDrops(level.getBlockState(pos), level, pos, level.getBlockEntity(pos))) Block.popResource(level, pos, drop); level.destroyBlock(pos, false); }
    }

    public List<ItemStack> drainStoredItems() { List<ItemStack> result = new ArrayList<>(); for (int i=0;i<storedItems.getSlots();i++) { ItemStack s=storedItems.getStackInSlot(i); if (!s.isEmpty()) result.add(s.copy()); } storedItems.setSize(0); return result; }
    public void onWandRemoved() { if (level instanceof ServerLevel server && wand.is(ModItems.LIGHT_WAND.get())) removePlacedLights(server); if (wand.is(ModItems.SPONGE_WAND.get()) || wand.is(ModItems.LAVASPONGE_WAND.get())) fluidTank.setFluid(FluidStack.EMPTY); }
    private void setChangedAndSync() { setChanged(); if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL); }

    @Override public void setRemoved() { if (level instanceof ServerLevel server) removePlacedLights(server); super.setRemoved(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { CompoundTag tag = new CompoundTag(); if (!wand.isEmpty()) tag.put(WAND_TAG, wand.save(registries)); return tag; }
    @Override public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) { wand = tag.contains(WAND_TAG, Tag.TAG_COMPOUND) ? ItemStack.parse(registries, tag.get(WAND_TAG)).orElse(ItemStack.EMPTY) : ItemStack.EMPTY; }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) { handleUpdateTag(packet.getTag(), registries); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); if (!wand.isEmpty()) tag.put(WAND_TAG, wand.save(registries)); tag.put("StoredItems", storedItems.serializeNBT(registries)); tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag())); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); wand = tag.contains(WAND_TAG, Tag.TAG_COMPOUND) ? ItemStack.parse(registries, tag.get(WAND_TAG)).orElse(ItemStack.EMPTY) : ItemStack.EMPTY; if (tag.contains("StoredItems", Tag.TAG_COMPOUND)) storedItems.deserializeNBT(registries, tag.getCompound("StoredItems")); if (tag.contains("FluidTank", Tag.TAG_COMPOUND)) fluidTank.readFromNBT(registries, tag.getCompound("FluidTank")); }
}
