package net.bennysmith.simplywands;

import com.mojang.logging.LogUtils;
import net.bennysmith.simplywands.item.ModCreativeModeTabs;
import net.bennysmith.simplywands.item.ModItems;
import net.bennysmith.simplywands.item.custom.OreLocatorWand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(simplywands.MOD_ID)
public class simplywands {
    public static final String MOD_ID = "simplywands";
    private static final Logger LOGGER = LogUtils.getLogger();

    public simplywands(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        NeoForge.EVENT_BUS.register(UpdateChecker.class);

        NeoForge.EVENT_BUS.register(OreLocatorWand.class);
        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Add creative tab contents here
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
