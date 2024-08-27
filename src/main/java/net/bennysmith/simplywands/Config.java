package net.bennysmith.simplywands;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = simplywands.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Config for UpdateChecker
    private static final ModConfigSpec.BooleanValue ENABLE_UPDATE_CHECKER = BUILDER
            .comment("Whether to enable the update checker")
            .define("enableUpdateChecker", true);

    // Config for Ore Locator Wand
    private static final ModConfigSpec.IntValue HIGHLIGHT_RADIUS = BUILDER
            .comment("The radius (in blocks) in which ores will be highlighted")
            .comment("Can easily crash if high!")
            .comment("Default: 8")
            .defineInRange("highlightRadius", 8, 1, 64);

    private static final ModConfigSpec.LongValue HIGHLIGHT_DURATION_MS = BUILDER
            .comment("The duration (in milliseconds) for which ores will be highlighted")
            .comment("Default: 30000")
            .defineInRange("highlightDurationMs", 30000L, 1000L, 60000L);

    // Public static fields for easy access
    public static boolean enableUpdateChecker;
    public static int highlightRadius;
    public static long highlightDurationMs;

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // Load config for UpdateChecker
        enableUpdateChecker = ENABLE_UPDATE_CHECKER.get();

        // Load config for Ore Locator Wand
        highlightRadius = HIGHLIGHT_RADIUS.get();
        highlightDurationMs = HIGHLIGHT_DURATION_MS.get();
    }
}
