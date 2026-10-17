package net.bennysmith.simplywands.util;

import net.bennysmith.simplywands.simplywands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> ACCELERATABLE_BLOCKS = createTag("acceleratable_blocks");
        public static final TagKey<Block> NON_ACCELERATABLE_BLOCKS = createTag("non_acceleratable_blocks");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(simplywands.MOD_ID, name));
        }
    }

    public static class Enchantments {
        /**
         * The enchantments a wand may actually receive. Anything outside this tag is refused by
         * both the anvil and the enchanting table, so a book is never wasted on a wand.
         */
        public static final TagKey<Enchantment> ALLOWED_ON_WANDS =
                TagKey.create(Registries.ENCHANTMENT,
                        ResourceLocation.fromNamespaceAndPath(simplywands.MOD_ID, "allowed_on_wands"));

        /**
         * Extra enchantments that only make sense on a wand that breaks blocks for a living.
         * Fortune and Silk Touch only reach the loot table through the Vein Miner Wand's drop
         * call, so no other wand is allowed to take them.
         */
        public static final TagKey<Enchantment> VEIN_MINER_ONLY_ENCHANTMENTS =
                TagKey.create(Registries.ENCHANTMENT,
                        ResourceLocation.fromNamespaceAndPath(simplywands.MOD_ID, "vein_miner_only_enchantments"));

        private Enchantments() {
        }
    }

    public static class Items {
        /**
         * The wands that read their Efficiency level and scale their effect with it. A wand that
         * is not in this tag must not be allowed to take Efficiency, because the enchantment
         * would sit on it doing nothing.
         */
        public static final TagKey<Item> EFFICIENCY_CAPABLE_WANDS =
                TagKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(simplywands.MOD_ID, "efficiency_capable_wands"));

        private Items() {
        }
    }

}
