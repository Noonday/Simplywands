package net.bennysmith.simplywands;

import com.mojang.logging.LogUtils;
import net.bennysmith.simplywands.client.WandScrollHandler;
import net.bennysmith.simplywands.item.ModCreativeModeTabs;
import net.bennysmith.simplywands.item.ModItems;
import net.bennysmith.simplywands.network.ChangeLightLevelPayload;
import net.bennysmith.simplywands.network.ClientPayloadHandler;
import net.bennysmith.simplywands.network.HighlightOresPayload;
import net.bennysmith.simplywands.network.ServerPayloadHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;

@Mod(simplywands.MOD_ID)
public class simplywands {
    public static final String MOD_ID = "simplywands";
    private static final Logger LOGGER = LogUtils.getLogger();

    public simplywands(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::onRegisterPayloadHandlers);

        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Common setup code
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Client-only setup code
        event.enqueueWork(() -> {
            NeoForge.EVENT_BUS.register(net.bennysmith.simplywands.client.OreLocatorWandRenderer.class);
            NeoForge.EVENT_BUS.register(new WandScrollHandler());
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Add creative tab contents here
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Server starting event handler
    }

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1"); // Protocol version
        registrar.playToClient(
                HighlightOresPayload.TYPE,
                HighlightOresPayload.CODEC,
                ClientPayloadHandler::handleHighlightOres
        );
        registrar.playToServer(
            ChangeLightLevelPayload.TYPE,
            ChangeLightLevelPayload.CODEC,
            ServerPayloadHandler::handleChangeLightLevel
        );
    }
}
