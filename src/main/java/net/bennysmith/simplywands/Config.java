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
    // Config for UpdateChecker
    public static boolean enableUpdateChecker;

    static final ModConfigSpec SPEC = BUILDER.build();


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // Config for UpdateChecker
        enableUpdateChecker = ENABLE_UPDATE_CHECKER.get();
    }
}
