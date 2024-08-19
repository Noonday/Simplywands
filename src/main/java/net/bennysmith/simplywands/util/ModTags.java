package net.bennysmith.simplywands.util;

import net.bennysmith.simplywands.simplywands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> ACCELERATABLE_BLOCKS = createTag("acceleratable_blocks");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(simplywands.MOD_ID, name));
        }
    }

    /*
    public static class Items {
        public static final TagKey<Item> ACCELERATABLE_ITEMS = createTag("acceleratable_items");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(simplywands.MOD_ID, name));
        }
    }
     */

}
