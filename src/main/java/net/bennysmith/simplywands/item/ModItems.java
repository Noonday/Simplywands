package net.bennysmith.simplywands.item;

import net.bennysmith.simplywands.item.custom.*;
import net.bennysmith.simplywands.simplywands;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


//  Items
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(simplywands.MOD_ID);

    //  Registered Wand Items
    public static final DeferredItem<Item> SPONGE_WAND =
            ITEMS.registerItem("sponge_wand", SpongeWand::new, new Item.Properties().durability(256));

    public static final DeferredItem<Item> LAVASPONGE_WAND =
            ITEMS.registerItem("lavasponge_wand", LavaSpongeWand::new, new Item.Properties().durability(512));

    public static final DeferredItem<Item> GROWTH_WAND =
            ITEMS.registerItem("growth_wand", GrowthWand::new, new Item.Properties().durability(128));

    public static final DeferredItem<Item> MAGNETIC_WAND =
            ITEMS.registerItem("magnetic_wand", MagneticWand::new, new Item.Properties().stacksTo(1)); // Stack size of 1 instead of durability

    public static final DeferredItem<Item> TELEPORT_WAND =
            ITEMS.registerItem("teleport_wand", TeleportWand::new, new Item.Properties().durability(10));

    public static final DeferredItem<Item> LIGHT_WAND =
            ITEMS.registerItem("light_wand", LightWand::new, new Item.Properties().durability(64));

    public static final DeferredItem<Item> ACCELERATION_WAND =
            ITEMS.registerItem("acceleration_wand", AccelerationWand::new, new Item.Properties().durability(32));

    public static final DeferredItem<Item> LOVE_WAND =
            ITEMS.registerItem("love_wand", LoveWand::new, new Item.Properties().durability(64));

    public static final DeferredItem<Item> ORELOCATOR_WAND =
            ITEMS.registerItem("orelocator_wand", OreLocatorWand::new, new Item.Properties().durability(32));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
