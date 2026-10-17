package net.bennysmith.simplywands.blockentity;

import net.bennysmith.simplywands.Config;
import net.bennysmith.simplywands.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class PedestalBlockEntity extends BlockEntity {
    public static final String WAND_TAG = "Wand";
    private static final int ITEM_SLOTS = 3;
    private static final int MAX_FLUID_BUCKETS = 32;
    //  How far a loose item has to reach the pedestal's own cube to be taken in. Bigger than half a
    //  block so an item pressed flat against a face still overlaps, small enough that an item in
    //  the next block along is left alone.
    private static final double MAGNET_CAPTURE_MARGIN = 0.75;
    private static final Map<UUID, PedestalBlockEntity> RESURRECTION_STATIONS = new ConcurrentHashMap<>();

    private ItemStack wand = ItemStack.EMPTY;
    private UUID owner;
    private int operationTicks;
    private int foodProgress;
    private boolean powered;
    private long nextTeleportTick;
    private boolean resurrectionChunkForced;
    private ServerLevel forcedChunkLevel;
    private BlockPos forcedChunkPos;
    private final Set<BlockPos> placedLights = ConcurrentHashMap.newKeySet();
    private final ItemStackHandler storedItems = new ItemStackHandler(ITEM_SLOTS) {
        @Override protected void onContentsChanged(int slot) { setChangedAndSync(); }
    };
    private final FluidTank fluidTank = new FluidTank(fluidCapacity(), this::acceptsFluid) {
        @Override protected void onContentsChanged() { setChangedAndSync(); }
    };

    //  The config is baked on ModConfigEvent, which can land after a block entity is
    //  built. Fall back to the full cap rather than handing out a zero-volume tank.
    private static int fluidCapacity() {
        int buckets = Config.pedestalFluidBuckets;
        return (buckets > 0 ? Math.min(buckets, MAX_FLUID_BUCKETS) : MAX_FLUID_BUCKETS) * 1000;
    }

    public PedestalBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.PEDESTAL.get(), pos, state); }
    public ItemStack getWand() { return wand; }
    public Component getWandDisplayName() { return wand.isEmpty() ? Component.empty() : wand.getHoverName(); }
    public ItemStackHandler getStoredItems() { return storedItems; }
    public IFluidHandler getFluidHandler() { return fluidTank; }
    public boolean hasWand(Item item) { return !wand.isEmpty() && wand.is(item); }
    public UUID getOwner() { return owner; }

    public void setWand(ItemStack stack) {
        if (!wand.isEmpty() && (stack.isEmpty() || wand.getItem() != stack.getItem())) clearWandResources();
        wand = stack;
        operationTicks = 0;
        if (!hasWand(ModItems.LIGHT_WAND.get())) clearLights();
        if (hasWand(ModItems.RESURRECTION_WAND.get())) wand.setCount(1);
        nextTeleportTick = 0;
        if (!hasWand(ModItems.RESURRECTION_WAND.get())) { unregisterResurrection(); releaseResurrectionChunk(); }
        updateResurrectionChunk();
        setChangedAndSync();
    }

    public void setWandFrom(Player player, ItemStack stack) { owner = player.getUUID(); setWand(stack); }
    public void clearWand() { setWand(ItemStack.EMPTY); owner = null; }
    public List<ItemStack> drainStoredItems() {
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < storedItems.getSlots(); i++) { ItemStack stack = storedItems.getStackInSlot(i); if (!stack.isEmpty()) result.add(stack.copy()); }
        for (int i = 0; i < storedItems.getSlots(); i++) storedItems.setStackInSlot(i, ItemStack.EMPTY);
        return result;
    }

    private boolean acceptsFluid(FluidStack stack) {
        return hasWand(ModItems.SPONGE_WAND.get()) && stack.is(Fluids.WATER)
                || hasWand(ModItems.LAVASPONGE_WAND.get()) && stack.is(Fluids.LAVA);
    }
    private boolean active() { return !wand.isEmpty() && !powered; }
    private void updatePower() {
        boolean now = level != null && level.hasNeighborSignal(worldPosition);
        if (now != powered) { powered = now; if (now) clearLights(); updateResurrectionChunk(); setChangedAndSync(); }
    }
    private void updateResurrectionChunk() {
        boolean shouldForce = active() && hasWand(ModItems.RESURRECTION_WAND.get()) && owner != null;
        if (shouldForce && level instanceof ServerLevel server) {
            BlockPos chunkPos = worldPosition;
            if (!resurrectionChunkForced || forcedChunkLevel != server || !chunkPos.equals(forcedChunkPos)) {
                releaseResurrectionChunk();
                server.setChunkForced(chunkPos.getX() >> 4, chunkPos.getZ() >> 4, true);
                forcedChunkLevel = server;
                forcedChunkPos = chunkPos.immutable();
                resurrectionChunkForced = true;
            }
        } else {
            releaseResurrectionChunk();
        }
    }
    private void releaseResurrectionChunk() {
        if (resurrectionChunkForced && forcedChunkLevel != null && forcedChunkPos != null) {
            forcedChunkLevel.setChunkForced(forcedChunkPos.getX() >> 4, forcedChunkPos.getZ() >> 4, false);
        }
        resurrectionChunkForced = false;
        forcedChunkLevel = null;
        forcedChunkPos = null;
    }
    private int configuredDurability() {
        Item item = wand.getItem();
        if (item == ModItems.SPONGE_WAND.get()) return Config.spongeWandDurability;
        if (item == ModItems.LAVASPONGE_WAND.get()) return Config.lavaSpongeWandDurability;
        if (item == ModItems.GROWTH_WAND.get()) return Config.growthWandDurability;
        if (item == ModItems.MAGNETIC_WAND.get()) return 0;
        if (item == ModItems.TELEPORT_WAND.get()) return Config.teleportWandDurability;
        if (item == ModItems.ACCELERATION_WAND.get()) return Config.accelerationWandDurability;
        if (item == ModItems.LOVE_WAND.get()) return Config.loveWandDurability;
        if (item == ModItems.ORELOCATOR_WAND.get()) return Config.oreLocatorWandDurability;
        if (item == ModItems.VEIN_MINER_WAND.get()) return Config.veinMinerWandDurability;
        if (item == ModItems.LIGHT_WAND.get()) return Config.lightWandDurability;
        return 0;
    }
    private void damageWand() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int durability = configuredDurability();
        if (durability <= 0) return;
        wand.setDamageValue(Math.min(wand.getDamageValue() + 1, durability));
        if (wand.getDamageValue() >= durability) clearWand();
        setChangedAndSync();
    }
    private void completedUse() { damageWand(); }

    //  Ticks between pedestal operations. Most wands just use the shared interval. The pedestal
    //  Sponge has no radius for Efficiency to widen - it works through a fluid tank instead - so
    //  Efficiency makes it act sooner rather than reach further. Never returns less than 1.
    private int operationInterval(ServerLevel server) {
        int interval = Math.max(1, Config.pedestalOperationInterval);
        if (hasWand(ModItems.SPONGE_WAND.get())) {
            int bonus = net.bennysmith.simplywands.item.custom.WandItem.efficiency(wand, server) * Config.pedestalSpongeEfficiencyInterval;
            interval = Math.max(1, interval - bonus);
        }
        return interval;
    }

    public void tickServer() {
        updatePower();
        updateResurrectionChunk();
        if (!(level instanceof ServerLevel server) || !active()) return;
        if (++operationTicks % operationInterval(server) != 0) return;
        Item item = wand.getItem();
        if (item == ModItems.SPONGE_WAND.get()) { collectFluid(server, Fluids.WATER, false); transferFluid(server); }
        else if (item == ModItems.LAVASPONGE_WAND.get()) { collectFluid(server, Fluids.LAVA, true); transferFluid(server); }
        else if (item == ModItems.MAGNETIC_WAND.get()) { attractItems(server); transferItems(server); }
        else if (item == ModItems.GROWTH_WAND.get()) { if (growCrops(server)) completedUse(); }
        else if (item == ModItems.ACCELERATION_WAND.get()) { if (accelerateBelow(server)) completedUse(); }
        else if (item == ModItems.LOVE_WAND.get()) { if (breedAnimals(server)) completedUse(); }
        else if (item == ModItems.FOOD_WAND.get()) feedPlayers(server);
        else if (item == ModItems.LIGHT_WAND.get()) maintainLights(server);
        else if (item == ModItems.VEIN_MINER_WAND.get()) { if (mineOres(server)) completedUse(); }
        else if (item == ModItems.RESURRECTION_WAND.get()) registerResurrection();
    }

    private void collectFluid(ServerLevel server, Fluid fluid, boolean connected) {
        if (fluidTank.getSpace() < 1000) return;
        List<BlockPos> candidates = connected ? connectedFluidSources(server, fluid) : adjacentFluidSources(server, fluid);
        candidates.sort(Comparator.comparingDouble((BlockPos p) -> p.distSqr(worldPosition)).reversed());
        if (candidates.isEmpty()) return;
        FluidStack stack = new FluidStack(fluid, 1000);
        if (fluidTank.fill(stack, IFluidHandler.FluidAction.SIMULATE) < 1000) return;
        BlockPos pos = candidates.get(0);
        server.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        fluidTank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        completedUse();
    }
    //  Water is only ever taken from the pedestal's own shell
    private List<BlockPos> adjacentFluidSources(ServerLevel server, Fluid fluid) {
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : new BlockPos[]{worldPosition.above(), worldPosition.below()}) {
            if (isSourceOf(server, pos, fluid)) found.add(pos.immutable());
        }
        return found;
    }
    //  Lava is drained from the body touching the pedestal, not from every pool in range: a
    //  separate pool across the room is not this wand's to empty. Bounded by the same cube the
    //  handheld wand searches.
    private List<BlockPos> connectedFluidSources(ServerLevel server, Fluid fluid) {
        int range = net.bennysmith.simplywands.item.custom.WandItem.scaledByEfficiency(wand, server, Config.lavaSpongeWandRange, Config.pedestalLavaSpongeEfficiencyRange);
        Set<BlockPos> reached = new LinkedHashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos pos : adjacentFluidSources(server, fluid)) {
            if (reached.add(pos)) queue.add(pos);
        }
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction side : Direction.values()) {
                BlockPos next = current.relative(side);
                if (reached.contains(next) || !inRange(worldPosition, next, range) || !isSourceOf(server, next, fluid)) continue;
                reached.add(next.immutable());
                queue.add(next);
            }
        }
        return new ArrayList<>(reached);
    }
    private static boolean inRange(BlockPos origin, BlockPos pos, int range) {
        return Math.abs(pos.getX() - origin.getX()) <= range
                && Math.abs(pos.getY() - origin.getY()) <= range
                && Math.abs(pos.getZ() - origin.getZ()) <= range;
    }
    private static boolean isSourceOf(ServerLevel server, BlockPos pos, Fluid fluid) {
        return server.getFluidState(pos).is(fluid) && server.getFluidState(pos).isSource();
    }
    private void transferFluid(ServerLevel server) {
        for (Direction side : Direction.values()) {
            if (fluidTank.isEmpty()) break;
            IFluidHandler target = server.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(side), side.getOpposite());
            if (target != null) { FluidStack offered=fluidTank.getFluid().copy(); int moved=target.fill(offered,IFluidHandler.FluidAction.EXECUTE); if(moved>0)fluidTank.drain(moved,IFluidHandler.FluidAction.EXECUTE); }
        }
    }
    private void attractItems(ServerLevel server) {
        //  The pedestal collides as a full cube, so the old target one block above its centre was
        //  buried inside the block. An item resting on the ground beside the pedestal sat ~1.3
        //  blocks from it, never crossed the insert radius, and the pull vector only pressed it
        //  into a face it could not pass. Aim at the block above instead, so items ride up over
        //  the pedestal, and take anything whose box actually reaches the pedestal.
        Vec3 target=Vec3.atCenterOf(worldPosition.above());
        AABB capture=new AABB(worldPosition).inflate(MAGNET_CAPTURE_MARGIN);
        for(ItemEntity entity:server.getEntitiesOfClass(ItemEntity.class,new AABB(worldPosition).inflate(Config.pedestalMagneticRange))) {
            Vec3 delta=target.subtract(entity.position());
            if(delta.lengthSqr()>.1)entity.setDeltaMovement(delta.normalize().scale(Config.pedestalMagnetSpeed));
            if(!entity.getBoundingBox().intersects(capture))continue;
            ItemStack before = entity.getItem().copy();
            ItemStack remainder=insertItem(before);
            if (remainder.getCount() < before.getCount()) completedUse();
            entity.setItem(remainder);
            if(remainder.isEmpty()) entity.discard();
        }
    }
    private ItemStack insertItem(ItemStack input) { ItemStack remainder=input.copy(); for(int i=0;i<storedItems.getSlots()&&!remainder.isEmpty();i++)remainder=storedItems.insertItem(i,remainder,false); return remainder; }
    private void transferItems(ServerLevel server) {
        for(Direction side:Direction.values()){ net.neoforged.neoforge.items.IItemHandler target=server.getCapability(Capabilities.ItemHandler.BLOCK,worldPosition.relative(side),side.getOpposite()); if(target==null)continue; for(int i=0;i<storedItems.getSlots();i++){ItemStack rem=storedItems.getStackInSlot(i).copy();for(int j=0;j<target.getSlots()&&!rem.isEmpty();j++)rem=target.insertItem(j,rem,false);storedItems.setStackInSlot(i,rem);}}
    }
    private boolean growCrops(ServerLevel server){boolean changed=false;int r=net.bennysmith.simplywands.item.custom.WandItem.scaledByEfficiency(wand,server,Config.pedestalGrowthRange,Config.pedestalGrowthEfficiencyRange);for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){BlockPos p=worldPosition.offset(x,0,z);BlockState s=server.getBlockState(p);if(s.getBlock() instanceof CropBlock crop&&crop.isValidBonemealTarget(server,p,s)){crop.performBonemeal(server,server.random,p,s);changed=true;}}return changed;}
    @SuppressWarnings("unchecked") private boolean accelerateBelow(ServerLevel server){int ticks=net.bennysmith.simplywands.item.custom.WandItem.scaledByEfficiency(wand,server,Config.pedestalAccelerationMultiplier,Config.pedestalAccelerationEfficiencyTicks);BlockPos p=worldPosition.below();BlockState s=server.getBlockState(p);boolean worked=false;if(s.isRandomlyTicking()){for(int i=0;i<ticks;i++)s.randomTick(server,p,server.random);worked=true;}BlockEntity e=server.getBlockEntity(p);if(e!=null){BlockEntityTicker<BlockEntity> t=(BlockEntityTicker<BlockEntity>)s.getTicker(server,e.getType());if(t!=null){for(int i=0;i<ticks;i++)t.tick(server,p,s,e);worked=true;}}return worked;}
    private boolean breedAnimals(ServerLevel server){List<Animal>a=server.getEntitiesOfClass(Animal.class,new AABB(worldPosition).inflate(net.bennysmith.simplywands.item.custom.WandItem.scaledByEfficiency(wand,server,Config.pedestalLoveRange,Config.pedestalLoveEfficiencyRange)),e->e.getAge()==0&&e.canFallInLove());for(int i=0;i+1<a.size();i+=2){a.get(i).setInLove(null);a.get(i+1).setInLove(null);}return a.size()>1;}
    private void feedPlayers(ServerLevel server){int perDamage=Config.pedestalFoodUnitsPerDamage;for(Player p:server.getEntitiesOfClass(Player.class,new AABB(worldPosition).inflate(Config.pedestalFoodRange))){int before=p.getFoodData().getFoodLevel();p.getFoodData().eat(Config.pedestalFoodAmount,(float)Config.pedestalSaturation);foodProgress+=p.getFoodData().getFoodLevel()-before;if(foodProgress>=perDamage){foodProgress-=perDamage;damageWand();}}}
    private void maintainLights(ServerLevel server){
        int level=Config.pedestalLightLevel;
        if(!placedLights.isEmpty()&&everyOwnedCellStillLit(server,level))return;
        clearLights();
        int spacing=Math.max(1,Math.min(4,level+1));
        int radius=Math.max(1,Config.pedestalLightRadius);
        int budget=Math.max(1,Config.pedestalLightMaxBlocks);
        //  Nearest cell first so the budget carves a centred ball rather than one lobe of it
        List<BlockPos> cells=new ArrayList<>();
        for(int x=-radius;x<=radius;x+=spacing)for(int y=-radius;y<=radius;y+=spacing)for(int z=-radius;z<=radius;z+=spacing){
            if(x*x+y*y+z*z>radius*radius)continue;
            BlockPos pos=worldPosition.offset(x,y,z);
            if(pos.equals(worldPosition))continue;
            //  A real block is never replaced, and the pedestal itself is always skipped above
            if(server.hasChunkAt(pos)&&server.getBlockState(pos).isAir())cells.add(pos.immutable());
        }
        cells.sort(Comparator.comparingDouble(p->p.distSqr(worldPosition)));
        BlockState light=Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,level);
        for(BlockPos pos:cells){if(placedLights.size()>=budget)break;server.setBlock(pos,light,Block.UPDATE_CLIENTS);placedLights.add(pos);}
    }
    private boolean everyOwnedCellStillLit(ServerLevel server,int level){
        for(BlockPos pos:placedLights)if(!server.hasChunkAt(pos)||!server.getBlockState(pos).is(Blocks.LIGHT)||server.getBlockState(pos).getValue(LightBlock.LEVEL)!=level)return false;
        return true;
    }
    private void clearLights(){if(level instanceof ServerLevel s)for(BlockPos p:placedLights)if(s.hasChunkAt(p)&&s.getBlockState(p).is(Blocks.LIGHT))s.setBlock(p,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);placedLights.clear();}
    //  The wand has to be handed to the loot context as the tool, otherwise Fortune and Silk
    //  Touch never reach the drop table and the pedestal mines worse than the handheld wand.
    //  The owner is passed as the entity when they are online so loot tables that look at
    //  this_entity still behave.
    private net.minecraft.world.entity.Entity ownerPlayer(ServerLevel server){
        if(owner==null)return null;
        return server.getServer().getPlayerList().getPlayer(owner);
    }
    private boolean mineOres(ServerLevel server){int r=Config.pedestalVeinRange,n=0;int max=net.bennysmith.simplywands.item.custom.WandItem.scaledByEfficiency(wand,server,Config.pedestalVeinMaxBlocks,Config.pedestalVeinEfficiencyBlocks);for(int x=-r;x<=r&&n<max;x++)for(int y=-r;y<=r&&n<max;y++)for(int z=-r;z<=r&&n<max;z++){if(x*x+y*y+z*z>r*r)continue;BlockPos p=worldPosition.offset(x,y,z);if(server.hasChunkAt(p)&&server.getBlockState(p).is(net.neoforged.neoforge.common.Tags.Blocks.ORES)){for(ItemStack d:Block.getDrops(server.getBlockState(p),server,p,server.getBlockEntity(p),ownerPlayer(server),wand))Block.popResource(server,p,d);server.destroyBlock(p,false);n++;}}return n>0;}

    public void registerResurrection(){if(owner!=null&&hasWand(ModItems.RESURRECTION_WAND.get())&&active()){RESURRECTION_STATIONS.put(owner,this);updateResurrectionChunk();}}
    public void unregisterResurrection(){if(owner!=null)RESURRECTION_STATIONS.remove(owner,this);releaseResurrectionChunk();}
    public static boolean resurrect(Player player) {
        if (!Config.pedestalResurrectionEnabled) return false;
        PedestalBlockEntity pedestal = RESURRECTION_STATIONS.get(player.getUUID());
        if (pedestal == null || !pedestal.active()) return false;
        ServerLevel level = pedestal.level instanceof ServerLevel server ? server : null;
        if (level == null) return false;
        boolean sameDimension = level.dimension().equals(player.level().dimension());
        if (!sameDimension) {
            if (!Config.pedestalResurrectionCrossDimensionEnabled || !(player instanceof ServerPlayer)) return false;
        }
        //  Same safety check as a teleport destination - never rescue into a solid block
        BlockPos destination = pedestal.worldPosition.above();
        if (!isFree(level, destination) || !isFree(level, destination.above())) return false;
        if (sameDimension) {
            player.teleportTo(destination.getX() + .5, destination.getY(), destination.getZ() + .5);
        } else {
            ((ServerPlayer) player).teleportTo(level, destination.getX() + .5, destination.getY(), destination.getZ() + .5,
                    Set.of(), player.getYRot(), player.getXRot());
        }
        player.setHealth(player.getMaxHealth());
        player.setAirSupply(player.getMaxAirSupply());
        player.removeAllEffects();
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 900, 1));
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 100, 1));
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 800, 0));
        player.getCombatTracker().recheckStatus();
        player.setInvulnerable(true);
        level.getServer().execute(() -> player.setInvulnerable(false));
        player.setAirSupply(player.getMaxAirSupply());
        player.setHealth(player.getMaxHealth());
        player.fallDistance = 0.0F;
        player.clearFire();
        level.playSound(null, destination, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1, 1);
        pedestal.clearWand();
        return true;
    }
    private static boolean isFree(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }
    public boolean teleport(Player player){
        if(!hasWand(ModItems.TELEPORT_WAND.get())||!active()||!(level instanceof ServerLevel server))return false;
        if(!server.dimension().equals(player.level().dimension()))return false;
        return teleportPlayerToStation(player);
    }
    public boolean teleportPlayerToStation(Player player) {
        if (!hasWand(ModItems.TELEPORT_WAND.get()) || !active() || !(level instanceof ServerLevel server)) return false;
        if (!server.dimension().equals(player.level().dimension()) || server.getGameTime() < nextTeleportTick) return false;
        if (player.blockPosition().distSqr(worldPosition) > 100 * 100) return false;
        BlockPos destination = worldPosition.above();
        if (!isFree(server, destination) || !isFree(server, destination.above())) return false;
        player.teleportTo(destination.getX() + .5, destination.getY(), destination.getZ() + .5);
        nextTeleportTick = server.getGameTime() + Math.max(0, Config.pedestalTeleportCooldownTicks);
        ItemStack teleportStack = player.getMainHandItem().is(ModItems.TELEPORT_WAND.get())
                ? player.getMainHandItem() : player.getOffhandItem();
        if (Config.teleportWandDurability > 0 && !teleportStack.isEmpty()) {
            teleportStack.hurtAndBreak(1, server, player instanceof ServerPlayer serverPlayer ? serverPlayer : null, broken -> { });
        }
        setChangedAndSync();
        return true;
    }
    public void highlight(Player player, Block target){if(!(player instanceof net.minecraft.server.level.ServerPlayer sp)||!(level instanceof ServerLevel s))return;List<BlockPos> found=new ArrayList<>();int r=Config.pedestalOreRadius;for(int x=-r;x<=r;x++)for(int y=-r;y<=r;y++)for(int z=-r;z<=r;z++){if(x*x+y*y+z*z<=r*r){BlockPos p=worldPosition.offset(x,y,z);if(s.hasChunkAt(p)&&s.getBlockState(p).is(target))found.add(p.immutable());}}net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,new net.bennysmith.simplywands.network.HighlightOresPayload(found));if(!found.isEmpty())completedUse();}
    private void clearWandResources(){clearLights();if(hasWand(ModItems.SPONGE_WAND.get())||hasWand(ModItems.LAVASPONGE_WAND.get()))fluidTank.setFluid(FluidStack.EMPTY);unregisterResurrection();}
    private void setChangedAndSync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_ALL);}
    //  setRemoved also runs on chunk unload, so the light cells are left in place there and
    //  their ownership travels with the save instead of being destroyed mid-unload
    @Override public void setRemoved(){unregisterResurrection();super.setRemoved();}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider p){CompoundTag t=new CompoundTag();if(!wand.isEmpty())t.put(WAND_TAG,wand.save(p));return t;}
    @Override public void handleUpdateTag(CompoundTag t,HolderLookup.Provider p){wand=t.contains(WAND_TAG,Tag.TAG_COMPOUND)?ItemStack.parse(p,t.get(WAND_TAG)).orElse(ItemStack.EMPTY):ItemStack.EMPTY;}
    @Override public void onDataPacket(Connection c,ClientboundBlockEntityDataPacket packet,HolderLookup.Provider p){handleUpdateTag(packet.getTag(),p);}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider p){super.saveAdditional(t,p);if(!wand.isEmpty())t.put(WAND_TAG,wand.save(p));t.put("StoredItems",storedItems.serializeNBT(p));t.put("FluidTank",fluidTank.writeToNBT(p,new CompoundTag()));if(owner!=null)t.putUUID("Owner",owner);t.putInt("FoodProgress",foodProgress);ListTag cells=new ListTag();for(BlockPos pos:placedLights){CompoundTag entry=new CompoundTag();entry.putInt("X",pos.getX());entry.putInt("Y",pos.getY());entry.putInt("Z",pos.getZ());cells.add(entry);}t.put("PlacedLights",cells);}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider p){super.loadAdditional(t,p);wand=t.contains(WAND_TAG,Tag.TAG_COMPOUND)?ItemStack.parse(p,t.get(WAND_TAG)).orElse(ItemStack.EMPTY):ItemStack.EMPTY;if(t.contains("StoredItems",Tag.TAG_COMPOUND))storedItems.deserializeNBT(p,t.getCompound("StoredItems"));
        //  1.8.2 saves carry the old 27-slot layout and ItemStackHandler honours the saved size
        if(storedItems.getSlots()!=ITEM_SLOTS)storedItems.setSize(ITEM_SLOTS);if(t.contains("FluidTank",Tag.TAG_COMPOUND))fluidTank.readFromNBT(p,t.getCompound("FluidTank"));owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;foodProgress=t.getInt("FoodProgress");placedLights.clear();if(t.contains("PlacedLights",Tag.TAG_LIST)){ListTag cells=t.getList("PlacedLights",Tag.TAG_COMPOUND);for(int i=0;i<cells.size();i++){CompoundTag entry=cells.getCompound(i);placedLights.add(new BlockPos(entry.getInt("X"),entry.getInt("Y"),entry.getInt("Z")));}}}
}
