package net.bennysmith.simplywands;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = simplywands.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();


    // Sponge Wand group
    static {
        BUILDER.push("Sponge Wand");
    }

    private static final ModConfigSpec.IntValue SPONGE_WAND_RANGE = BUILDER
            .comment("The range (in blocks) in which the Sponge Wand will remove water")
            .comment("This determines the radius of the cube area centered on the player")
            .comment("Default: 3 (7x7x5 area)")
            .defineInRange("spongeWandRange", 3, 1, 10);

    static {
        BUILDER.pop();
    }

    // Lava Sponge Wand group
    static {
        BUILDER.push("Lava Sponge Wand");
    }

    private static final ModConfigSpec.IntValue LAVA_SPONGE_WAND_RANGE = BUILDER
            .comment("The range (in blocks) in which the Lava Sponge Wand will remove lava")
            .comment("This determines the radius of the cube area centered on the player")
            .comment("Default: 2 (5x5x5 area)")
            .defineInRange("lavaSpongeWandRange", 2, 1, 5);

    static {
        BUILDER.pop();
    }

    // Growth Wand group
    static {
        BUILDER.push("Growth Wand");
    }

    private static final ModConfigSpec.IntValue GROWTH_WAND_RANGE = BUILDER
            .comment("The range (in blocks) in which the Growth Wand will affect crops")
            .comment("This determines the radius of the square area centered on the target block")
            .comment("Default: 3 (7x7 area)")
            .defineInRange("growthWandRange", 3, 1, 10);

    static {
        BUILDER.pop();
    }

    // Magnetic Wand group
    static {
        BUILDER.push("Magnetic Wand");
    }

    private static final ModConfigSpec.DoubleValue MAGNETIC_WAND_RANGE = BUILDER
            .comment("The range (in blocks) in which the Magnetic Wand will attract items")
            .comment("Default: 5.0")
            .defineInRange("magneticWandRange", 5.0, 1.0, 32.0);

    private static final ModConfigSpec.DoubleValue MAGNETIC_WAND_SPEED = BUILDER
            .comment("The speed at which items are attracted to the player")
            .comment("Default: 0.5")
            .defineInRange("magneticWandSpeed", 0.5, 0.1, 2.0);

    static {
        BUILDER.pop();
    }

    // Teleport Wand group
    static {
        BUILDER.push("Teleport Wand");
    }

    private static final ModConfigSpec.IntValue TELEPORT_WAND_DISTANCE = BUILDER
            .comment("The maximum distance (in blocks) the Teleport Wand can teleport the player")
            .comment("Default: 10")
            .defineInRange("teleportWandDistance", 10, 1, 50);

    static {
        BUILDER.pop();
    }

    // Acceleration Wand group
    static {
        BUILDER.push("Acceleration Wand");
    }

    private static final ModConfigSpec.IntValue ACCELERATION_TICK_MULTIPLIER = BUILDER
            .comment("The number of extra ticks applied when accelerating a block")
            .comment("Default: 256")
            .defineInRange("accelerationTickMultiplier", 256, 1, 1000);

    static {
        BUILDER.pop();
    }

    // Love Wand group
    static {
        BUILDER.push("Love Wand");
    }

    private static final ModConfigSpec.DoubleValue LOVE_WAND_LURE_RANGE = BUILDER
            .comment("The maximum range (in blocks) at which the Love Wand can lure animals")
            .comment("Default: 7.0")
            .defineInRange("loveWandLureRange", 7.0, 1.0, 16.0);

    static {
        BUILDER.pop();
    }

    // Ore-Locator Wand group
    static {
        BUILDER.push("Ore-Locator Wand");
    }

    private static final ModConfigSpec.IntValue HIGHLIGHT_RADIUS = BUILDER
            .comment("The radius (in blocks) in which ores will be highlighted")
            .comment("Default: 16")
            .defineInRange("highlightRadius", 16, 1, 64);

    private static final ModConfigSpec.LongValue HIGHLIGHT_DURATION_MS = BUILDER
            .comment("The duration (in milliseconds) for which ores will be highlighted")
            .comment("Default: 30000")
            .defineInRange("highlightDurationMs", 30000L, 1000L, 60000L);

    static {
        BUILDER.pop();
    }

    // Vein Miner Wand group
    static {
        BUILDER.push("Vein Miner Wand");
    }

    private static final ModConfigSpec.IntValue VEIN_MINER_MAX_BLOCKS = BUILDER
            .comment("The maximum number of blocks that the Vein Miner Wand can break in one use")
            .comment("Default: 64")
            .defineInRange("veinMinerMaxBlocks", 64, 1, 512);

    static {
        BUILDER.pop();
    }

    // Public static fields for easy access
    public static int highlightRadius;
    public static long highlightDurationMs;
    public static int accelerationTickMultiplier;
    public static int growthWandRange;
    public static int lavaSpongeWandRange;
    public static double loveWandLureRange;
    public static double magneticWandRange;
    public static double magneticWandSpeed;
    public static int spongeWandRange;
    public static int teleportWandDistance;
    public static int veinMinerMaxBlocks;

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {

        // Load config for Ore Locator Wand
        highlightRadius = HIGHLIGHT_RADIUS.get();
        highlightDurationMs = HIGHLIGHT_DURATION_MS.get();

        // Load config for Acceleration Wand
        accelerationTickMultiplier = ACCELERATION_TICK_MULTIPLIER.get();

        // Load config for Growth Wand
        growthWandRange = GROWTH_WAND_RANGE.get();

        // Load config for Lava Sponge Wand
        lavaSpongeWandRange = LAVA_SPONGE_WAND_RANGE.get();

        // Load config for Love Wand
        loveWandLureRange = LOVE_WAND_LURE_RANGE.get();

        // Load config for Magnetic Wand
        magneticWandRange = MAGNETIC_WAND_RANGE.get();
        magneticWandSpeed = MAGNETIC_WAND_SPEED.get();

        // Load config for Sponge Wand
        spongeWandRange = SPONGE_WAND_RANGE.get();

        // Load config for Teleport Wand
        teleportWandDistance = TELEPORT_WAND_DISTANCE.get();

        // Load config for Vein Miner Wand
        veinMinerMaxBlocks = VEIN_MINER_MAX_BLOCKS.get();
    }
}
