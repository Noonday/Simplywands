package net.bennysmith.simplywands;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

@EventBusSubscriber(modid = simplywands.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    public static final ServerConfig SERVER;
    public static final ModConfigSpec SERVER_SPEC;

    static {
        final Pair<ServerConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static class ServerConfig {
        // Config options for each wand
        public final ModConfigSpec.IntValue spongeWandRange;
        public final ModConfigSpec.IntValue spongeWandDurability;
        public final ModConfigSpec.IntValue lavaSpongeWandRange;
        public final ModConfigSpec.IntValue lavaSpongeWandDurability;
        public final ModConfigSpec.IntValue growthWandRange;
        public final ModConfigSpec.IntValue growthWandDurability;
        public final ModConfigSpec.DoubleValue magneticWandRange;
        public final ModConfigSpec.DoubleValue magneticWandSpeed;
        public final ModConfigSpec.IntValue teleportWandDistance;
        public final ModConfigSpec.IntValue teleportWandDurability;
        public final ModConfigSpec.IntValue accelerationTickMultiplier;
        public final ModConfigSpec.IntValue accelerationWandDurability;
        public final ModConfigSpec.DoubleValue loveWandLureRange;
        public final ModConfigSpec.IntValue loveWandDurability;
        public final ModConfigSpec.IntValue highlightRadius;
        public final ModConfigSpec.LongValue highlightDurationMs;
        public final ModConfigSpec.IntValue oreLocatorWandDurability;
        public final ModConfigSpec.IntValue veinMinerMaxBlocks;
        public final ModConfigSpec.IntValue veinMinerWandDurability;
        public final ModConfigSpec.IntValue lightWandDurability;
        public final ModConfigSpec.IntValue pedestalGrowthRange;
        public final ModConfigSpec.IntValue pedestalMagneticRange;
        public final ModConfigSpec.IntValue pedestalLoveRange;
        public final ModConfigSpec.IntValue pedestalFoodRange;
        public final ModConfigSpec.IntValue pedestalFoodAmount;
        public final ModConfigSpec.IntValue pedestalFoodUnitsPerDamage;
        public final ModConfigSpec.DoubleValue pedestalSaturation;
        public final ModConfigSpec.IntValue pedestalLightRadius;
        public final ModConfigSpec.IntValue pedestalLightMaxBlocks;
        public final ModConfigSpec.IntValue pedestalVeinRange;
        public final ModConfigSpec.IntValue pedestalVeinMaxBlocks;
        public final ModConfigSpec.IntValue pedestalTeleportRangeMultiplier;
        public final ModConfigSpec.IntValue pedestalTeleportCooldownTicks;
        public final ModConfigSpec.IntValue pedestalOreRadius;
        public final ModConfigSpec.BooleanValue pedestalResurrectionCrossDimensionEnabled;
        public final ModConfigSpec.IntValue pedestalAccelerationMultiplier;
        public final ModConfigSpec.IntValue pedestalOperationInterval;
        public final ModConfigSpec.IntValue pedestalFluidBuckets;
        public final ModConfigSpec.DoubleValue pedestalMagnetSpeed;
        public final ModConfigSpec.IntValue pedestalLightLevel;
        public final ModConfigSpec.BooleanValue pedestalResurrectionEnabled;

        ServerConfig(ModConfigSpec.Builder builder) {
            builder.comment("Simply Wands Configuration");

            //  Only settings shared by every pedestal wand live here. Each wand's own
            //  pedestal settings sit in that wand's section below.
            builder.push("Pedestals");
            pedestalOperationInterval = builder
                    .comment("The number of ticks between pedestal wand operations")
                    .defineInRange("operationIntervalTicks", 20, 1, 1200);
            pedestalFluidBuckets = builder
                    .comment("The number of buckets a pedestal-held sponge wand can hold")
                    .defineInRange("fluidCapacityBuckets", 32, 1, 32);
            builder.pop();


            builder.push("Sponge Wand");
            spongeWandRange = builder
                    .comment("The range (in blocks) in which the Sponge Wand will remove water",
                            "This determines the radius of the cube area centered on the player",
                            "Default: 3 (7x7x5 area)")
                    .defineInRange("spongeWandRange", 3, 1, 10);
            spongeWandDurability = builder
                    .comment("The durability of the Sponge Wand")
                    .comment("Default: 256 (0 = unlimited)")
                    .defineInRange("spongeWandDurability", 256, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Lava Sponge Wand");
            lavaSpongeWandRange = builder
                    .comment("The range (in blocks) in which the Lava Sponge Wand will remove lava",
                            "This determines the radius of the cube area centered on the player",
                            "Default: 2 (5x5x5 area)")
                    .defineInRange("lavaSpongeWandRange", 2, 1, 10);
            lavaSpongeWandDurability = builder
                    .comment("The durability of the Lava Sponge Wand")
                    .comment("Default: 512 (0 = unlimited)")
                    .defineInRange("lavaSpongeWandDurability", 512, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Growth Wand");
            growthWandRange = builder
                    .comment("The range (in blocks) in which the Growth Wand will affect crops",
                            "This determines the radius of the square area centered on the target block",
                            "Default: 3 (7x7 area)")
                    .defineInRange("growthWandRange", 3, 1, 10);
            growthWandDurability = builder
                    .comment("The durability of the Growth Wand")
                    .comment("Default: 128 (0 = unlimited)")
                    .defineInRange("growthWandDurability", 128, 0, Integer.MAX_VALUE);
            pedestalGrowthRange = builder
                    .comment("The radius (in blocks) in which a pedestal-held Growth Wand bonemeals crops")
                    .defineInRange("pedestalGrowthRange", 2, 1, 16);
            builder.pop();

            builder.push("Magnetic Wand");
            magneticWandRange = builder
                    .comment("The range (in blocks) in which the Magnetic Wand will attract items")
                    .defineInRange("magneticWandRange", 5.0, 1.0, 16.0);
            magneticWandSpeed = builder
                    .comment("The speed at which items are attracted to the player")
                    .defineInRange("magneticWandSpeed", 0.5, 0.1, 2.0);
            pedestalMagneticRange = builder
                    .comment("The radius (in blocks) in which a pedestal-held Magnetic Wand pulls items")
                    .defineInRange("pedestalMagneticRange", 10, 1, 64);
            pedestalMagnetSpeed = builder
                    .comment("The speed at which a pedestal-held Magnetic Wand pulls items")
                    .defineInRange("pedestalMagnetSpeed", 0.25, 0.05, 2.0);
            builder.pop();

            builder.push("Teleport Wand");
            teleportWandDistance = builder
                    .comment("The maximum distance (in blocks) the Teleport Wand can teleport the player")
                    .defineInRange("teleportWandDistance", 10, 1, 50);
            teleportWandDurability = builder
                    .comment("The durability of the Teleport Wand")
                    .comment("Default: 10 (0 = unlimited)")
                    .defineInRange("teleportWandDurability", 10, 0, Integer.MAX_VALUE);
            pedestalTeleportRangeMultiplier = builder
                    .comment("Multiplier applied to the Teleport Wand distance when travelling to a pedestal station")
                    .defineInRange("pedestalTeleportRangeMultiplier", 10, 1, 50);
            pedestalTeleportCooldownTicks = builder
                    .comment("The ticks a pedestal station must wait between teleports")
                    .defineInRange("pedestalTeleportCooldownTicks", 20, 0, 1200);
            builder.pop();

            builder.push("Light Wand");
            lightWandDurability = builder
                    .comment("The durability of the Light Wand")
                    .comment("Default: 64 (0 = unlimited)")
                    .defineInRange("lightWandDurability", 64, 0, Integer.MAX_VALUE);
            pedestalLightLevel = builder
                    .comment("The light level of each block a pedestal-held Light Wand places")
                    .defineInRange("pedestalLightLevel", 15, 1, 15);
            pedestalLightRadius = builder
                    .comment("The radius (in blocks) a pedestal-held Light Wand lights")
                    .defineInRange("pedestalLightRadius", 35, 1, 64);
            pedestalLightMaxBlocks = builder
                    .comment("The most light blocks a pedestal-held Light Wand may place")
                    .defineInRange("pedestalLightMaxBlocks", 256, 1, 4096);
            builder.pop();

            builder.push("Acceleration Wand");
            accelerationTickMultiplier = builder
                    .comment("The number of extra ticks applied when accelerating a block")
                    .defineInRange("accelerationTickMultiplier", 256, 1, 1000);
            accelerationWandDurability = builder
                    .comment("The durability of the Acceleration Wand")
                    .comment("Default: 48 (0 = unlimited)")
                    .defineInRange("accelerationWandDurability", 48, 0, Integer.MAX_VALUE);
            pedestalAccelerationMultiplier = builder
                    .comment("The number of extra ticks a pedestal-held Acceleration Wand applies")
                    .defineInRange("pedestalAccelerationMultiplier", 16, 1, 1000);
            builder.pop();

            builder.push("Love Wand");
            loveWandLureRange = builder
                    .comment("The maximum range (in blocks) at which the Love Wand can lure animals")
                    .defineInRange("loveWandLureRange", 7.0, 1.0, 16.0);
            loveWandDurability = builder
                    .comment("The durability of the Love Wand")
                    .comment("Default: 64 (0 = unlimited)")
                    .defineInRange("loveWandDurability", 64, 0, Integer.MAX_VALUE);
            pedestalLoveRange = builder
                    .comment("The radius (in blocks) in which a pedestal-held Love Wand pairs animals")
                    .defineInRange("pedestalLoveRange", 7, 1, 32);
            builder.pop();

            builder.push("Ore-Locator Wand");
            highlightRadius = builder
                    .comment("The radius (in blocks) in which ores will be highlighted")
                    .defineInRange("highlightRadius", 16, 1, 64);
            highlightDurationMs = builder
                    .comment("The duration (in milliseconds) for which ores will be highlighted")
                    .defineInRange("highlightDurationMs", 30000L, 1000L, 60000L);
            oreLocatorWandDurability = builder
                    .comment("The durability of the Ore-Locator Wand")
                    .comment("Default: 64 (0 = unlimited)")
                    .defineInRange("oreLocatorWandDurability", 64, 0, Integer.MAX_VALUE);
            pedestalOreRadius = builder
                    .comment("The radius (in blocks) a pedestal-held Ore-Locator Wand searches")
                    .defineInRange("pedestalOreRadius", 20, 1, 64);
            builder.pop();

            builder.push("Vein Miner Wand");
            veinMinerMaxBlocks = builder
                    .comment("The maximum number of blocks that the Vein Miner Wand can break in one use")
                    .defineInRange("veinMinerMaxBlocks", 64, 1, 512);
            veinMinerWandDurability = builder
                    .comment("The durability of the Vein Miner Wand")
                    .comment("Default: 1562 (0 = unlimited)")
                    .defineInRange("veinMinerWandDurability", 1562, 0, Integer.MAX_VALUE);
            pedestalVeinRange = builder
                    .comment("The radius (in blocks) a pedestal-held Vein Miner Wand searches")
                    .defineInRange("pedestalVeinRange", 10, 1, 32);
            pedestalVeinMaxBlocks = builder
                    .comment("The most blocks a pedestal-held Vein Miner Wand breaks in one operation")
                    .defineInRange("pedestalVeinMaxBlocks", 64, 1, 512);
            builder.pop();

            builder.push("Food Wand");
            pedestalFoodRange = builder
                    .comment("The radius (in blocks) in which a pedestal-held Food Wand feeds players")
                    .defineInRange("pedestalFoodRange", 7, 1, 32);
            pedestalFoodAmount = builder
                    .comment("The food points restored by one pedestal-held Food Wand operation")
                    .defineInRange("pedestalFoodAmount", 1, 1, 20);
            pedestalSaturation = builder
                    .comment("The saturation modifier used by a pedestal-held Food Wand")
                    .defineInRange("pedestalSaturation", 0.5, 0.0, 20.0);
            pedestalFoodUnitsPerDamage = builder
                    .comment("The food points granted before a pedestal-held Food Wand loses one durability")
                    .defineInRange("pedestalFoodUnitsPerDamage", 20, 1, 1000);
            builder.pop();

            builder.push("Resurrection Wand");
            pedestalResurrectionEnabled = builder
                    .comment("Whether a pedestal-held Resurrection Wand can rescue its owner")
                    .define("pedestalResurrectionEnabled", true);
            pedestalResurrectionCrossDimensionEnabled = builder
                    .comment("Whether a pedestal station can rescue its owner from another dimension")
                    .define("pedestalResurrectionCrossDimensionEnabled", false);
            builder.pop();
        }
    }

    // Static fields for easy access
    public static int spongeWandRange;
    public static int spongeWandDurability;
    public static int lavaSpongeWandRange;
    public static int lavaSpongeWandDurability;
    public static int growthWandRange;
    public static int growthWandDurability;
    public static double magneticWandRange;
    public static double magneticWandSpeed;
    public static int teleportWandDistance;
    public static int teleportWandDurability;
    public static int accelerationTickMultiplier;
    public static int accelerationWandDurability;
    public static double loveWandLureRange;
    public static int loveWandDurability;
    public static int highlightRadius;
    public static long highlightDurationMs;
    public static int oreLocatorWandDurability;
    public static int veinMinerMaxBlocks;
    public static int veinMinerWandDurability;
    public static int lightWandDurability;
    public static int pedestalGrowthRange;
    public static int pedestalMagneticRange;
    public static int pedestalLoveRange;
    public static int pedestalFoodRange;
    public static int pedestalFoodAmount;
    public static int pedestalFoodUnitsPerDamage;
    public static double pedestalSaturation;
    public static int pedestalLightRadius;
    public static int pedestalLightMaxBlocks;
    public static int pedestalVeinRange;
    public static int pedestalVeinMaxBlocks;
    public static int pedestalTeleportRangeMultiplier;
    public static int pedestalTeleportCooldownTicks;
    public static int pedestalOreRadius;
    public static boolean pedestalResurrectionCrossDimensionEnabled;
    public static int pedestalAccelerationMultiplier;
    public static int pedestalOperationInterval;
    public static int pedestalFluidBuckets;
    public static double pedestalMagnetSpeed;
    public static int pedestalLightLevel;
    public static boolean pedestalResurrectionEnabled;

    public static void bake() {
        spongeWandRange = SERVER.spongeWandRange.get();
        spongeWandDurability = SERVER.spongeWandDurability.get();
        lavaSpongeWandRange = SERVER.lavaSpongeWandRange.get();
        lavaSpongeWandDurability = SERVER.lavaSpongeWandDurability.get();
        growthWandRange = SERVER.growthWandRange.get();
        growthWandDurability = SERVER.growthWandDurability.get();
        magneticWandRange = SERVER.magneticWandRange.get();
        magneticWandSpeed = SERVER.magneticWandSpeed.get();
        teleportWandDistance = SERVER.teleportWandDistance.get();
        teleportWandDurability = SERVER.teleportWandDurability.get();
        accelerationTickMultiplier = SERVER.accelerationTickMultiplier.get();
        accelerationWandDurability = SERVER.accelerationWandDurability.get();
        loveWandLureRange = SERVER.loveWandLureRange.get();
        loveWandDurability = SERVER.loveWandDurability.get();
        highlightRadius = SERVER.highlightRadius.get();
        highlightDurationMs = SERVER.highlightDurationMs.get();
        oreLocatorWandDurability = SERVER.oreLocatorWandDurability.get();
        veinMinerMaxBlocks = SERVER.veinMinerMaxBlocks.get();
        veinMinerWandDurability = SERVER.veinMinerWandDurability.get();
        lightWandDurability = SERVER.lightWandDurability.get();
        pedestalGrowthRange = SERVER.pedestalGrowthRange.get();
        pedestalMagneticRange = SERVER.pedestalMagneticRange.get();
        pedestalLoveRange = SERVER.pedestalLoveRange.get();
        pedestalFoodRange = SERVER.pedestalFoodRange.get();
        pedestalFoodAmount = SERVER.pedestalFoodAmount.get();
        pedestalFoodUnitsPerDamage = SERVER.pedestalFoodUnitsPerDamage.get();
        pedestalSaturation = SERVER.pedestalSaturation.get();
        pedestalLightRadius = SERVER.pedestalLightRadius.get();
        pedestalLightMaxBlocks = SERVER.pedestalLightMaxBlocks.get();
        pedestalVeinRange = SERVER.pedestalVeinRange.get();
        pedestalVeinMaxBlocks = SERVER.pedestalVeinMaxBlocks.get();
        pedestalTeleportRangeMultiplier = SERVER.pedestalTeleportRangeMultiplier.get();
        pedestalTeleportCooldownTicks = SERVER.pedestalTeleportCooldownTicks.get();
        pedestalOreRadius = SERVER.pedestalOreRadius.get();
        pedestalResurrectionCrossDimensionEnabled = SERVER.pedestalResurrectionCrossDimensionEnabled.get();
        pedestalAccelerationMultiplier = SERVER.pedestalAccelerationMultiplier.get();
        pedestalOperationInterval = SERVER.pedestalOperationInterval.get();
        pedestalFluidBuckets = SERVER.pedestalFluidBuckets.get();
        pedestalMagnetSpeed = SERVER.pedestalMagnetSpeed.get();
        pedestalLightLevel = SERVER.pedestalLightLevel.get();
        pedestalResurrectionEnabled = SERVER.pedestalResurrectionEnabled.get();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent event) {
        // Skip unloading events, only process Loading and Reloading - Should hopefully fix the server shutdown issue.
        if (!(event instanceof ModConfigEvent.Unloading) && event.getConfig().getSpec() == SERVER_SPEC) {
            bake();
        }
    }
}
