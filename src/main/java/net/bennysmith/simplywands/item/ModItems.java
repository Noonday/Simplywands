package net.bennysmith.simplywands.item;

import net.bennysmith.simplywands.Config;
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
            ITEMS.registerItem("sponge_wand", SpongeWand::new, new Item.Properties().durability(Config.spongeWandDurability));

    public static final DeferredItem<Item> LAVASPONGE_WAND =
            ITEMS.registerItem("lavasponge_wand", LavaSpongeWand::new, new Item.Properties().durability(Config.lavaSpongeWandDurability));

    public static final DeferredItem<Item> GROWTH_WAND =
            ITEMS.registerItem("growth_wand", GrowthWand::new, new Item.Properties().durability(Config.growthWandDurability));

    public static final DeferredItem<Item> MAGNETIC_WAND =
            ITEMS.registerItem("magnetic_wand", MagneticWand::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> TELEPORT_WAND =
            ITEMS.registerItem("teleport_wand", TeleportWand::new, new Item.Properties().durability(Config.teleportWandDurability));

    public static final DeferredItem<Item> LIGHT_WAND =
            ITEMS.registerItem("light_wand", LightWand::new, new Item.Properties().durability(Config.lightWandDurability));

    public static final DeferredItem<Item> ACCELERATION_WAND =
            ITEMS.registerItem("acceleration_wand", AccelerationWand::new, new Item.Properties().durability(Config.accelerationWandDurability));

    public static final DeferredItem<Item> LOVE_WAND =
            ITEMS.registerItem("love_wand", LoveWand::new, new Item.Properties().durability(Config.loveWandDurability));

    public static final DeferredItem<Item> ORELOCATOR_WAND =
            ITEMS.registerItem("orelocator_wand", OreLocatorWand::new, new Item.Properties().durability(Config.oreLocatorWandDurability));

    public static final DeferredItem<Item> VEIN_MINER_WAND =
            ITEMS.registerItem("vein_miner_wand", VeinMinerWand::new, new Item.Properties().durability(Config.veinMinerWandDurability));

    public static final DeferredItem<Item> FOOD_WAND =
            ITEMS.registerItem("food_wand", FoodWand::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> RESURRECTION_WAND =
            ITEMS.registerItem("resurrection_wand", ResurrectionWand::new, new Item.Properties().stacksTo(1));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
