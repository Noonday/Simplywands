package net.bennysmith.simplywands.item;

import net.bennysmith.simplywands.simplywands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, simplywands.MOD_ID);


    //  Creative tab for SimplyWands
    public static final Supplier<CreativeModeTab> SIMPLY_WANDS = CREATIVE_MODE_TABS.register("simplywands", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.simplywands.simplywands_tab"))
            .icon(() -> new ItemStack(ModItems.SPONGE_WAND.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.SPONGE_WAND.get());
                output.accept(ModItems.LAVASPONGE_WAND.get());
                output.accept(ModItems.GROWTH_WAND.get());
                output.accept(ModItems.MAGNETIC_WAND.get());
                output.accept(ModItems.TELEPORT_WAND.get());
                output.accept(ModItems.LIGHT_WAND.get());
                output.accept(ModItems.ACCELERATION_WAND.get());
                output.accept(ModItems.LOVE_WAND.get());
                output.accept(ModItems.ORELOCATOR_WAND.get());
                output.accept(ModItems.VEIN_MINER_WAND.get());
                output.accept(ModItems.FOOD_WAND.get());

            }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
