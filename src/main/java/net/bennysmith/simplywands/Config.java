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

        ServerConfig(ModConfigSpec.Builder builder) {
            builder.comment("Simply Wands Configuration");

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
            builder.pop();

            builder.push("Magnetic Wand");
            magneticWandRange = builder
                    .comment("The range (in blocks) in which the Magnetic Wand will attract items")
                    .defineInRange("magneticWandRange", 5.0, 1.0, 16.0);
            magneticWandSpeed = builder
                    .comment("The speed at which items are attracted to the player")
                    .defineInRange("magneticWandSpeed", 0.5, 0.1, 2.0);
            builder.pop();

            builder.push("Teleport Wand");
            teleportWandDistance = builder
                    .comment("The maximum distance (in blocks) the Teleport Wand can teleport the player")
                    .defineInRange("teleportWandDistance", 10, 1, 50);
            teleportWandDurability = builder
                    .comment("The durability of the Teleport Wand")
                    .comment("Default: 10 (0 = unlimited)")
                    .defineInRange("teleportWandDurability", 10, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Light Wand");
            lightWandDurability = builder
                    .comment("The durability of the Light Wand")
                    .comment("Default: 64 (0 = unlimited)")
                    .defineInRange("lightWandDurability", 64, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Acceleration Wand");
            accelerationTickMultiplier = builder
                    .comment("The number of extra ticks applied when accelerating a block")
                    .defineInRange("accelerationTickMultiplier", 256, 1, 1000);
            accelerationWandDurability = builder
                    .comment("The durability of the Acceleration Wand")
                    .comment("Default: 48 (0 = unlimited)")
                    .defineInRange("accelerationWandDurability", 48, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Love Wand");
            loveWandLureRange = builder
                    .comment("The maximum range (in blocks) at which the Love Wand can lure animals")
                    .defineInRange("loveWandLureRange", 7.0, 1.0, 16.0);
            loveWandDurability = builder
                    .comment("The durability of the Love Wand")
                    .comment("Default: 64 (0 = unlimited)")
                    .defineInRange("loveWandDurability", 64, 0, Integer.MAX_VALUE);
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
            builder.pop();

            builder.push("Vein Miner Wand");
            veinMinerMaxBlocks = builder
                    .comment("The maximum number of blocks that the Vein Miner Wand can break in one use")
                    .defineInRange("veinMinerMaxBlocks", 64, 1, 512);
            veinMinerWandDurability = builder
                    .comment("The durability of the Vein Miner Wand")
                    .comment("Default: 1562 (0 = unlimited)")
                    .defineInRange("veinMinerWandDurability", 1562, 0, Integer.MAX_VALUE);
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
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent event) {
        // Skip unloading events, only process Loading and Reloading - Should hopefully fix the server shutdown issue.
        if (!(event instanceof ModConfigEvent.Unloading) && event.getConfig().getSpec() == SERVER_SPEC) {
            bake();
        }
    }
}
