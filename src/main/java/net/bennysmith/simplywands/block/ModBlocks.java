package net.bennysmith.simplywands.block;

import net.bennysmith.simplywands.simplywands;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

//  Blocks
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(simplywands.MOD_ID);

    //  Wand display pedestal - holds a single item that floats and spins above it
    public static final DeferredBlock<PedestalBlock> PEDESTAL =
            BLOCKS.registerBlock("pedestal", PedestalBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .sound(SoundType.STONE)
                    .strength(3.5F, 6.0F)
                    .noOcclusion());

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}