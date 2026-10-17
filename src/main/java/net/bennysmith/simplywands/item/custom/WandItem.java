package net.bennysmith.simplywands.item.custom;

import net.bennysmith.simplywands.util.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Shared base for every wand.
 *
 * <p>Vanilla only knows how to enchant the items it ships itself, so a wand has to opt in on
 * several separate gates before an enchanting table, an anvil or an enchanted book will touch
 * it. That policy lives here once rather than being repeated in every wand.
 *
 * <p>Only wands that can actually wear out accept enchantments. The indestructible wands -
 * Magnetic, Food and Resurrection - carry no durability, and Unbreaking or Mending on them
 * would do nothing at all, so they are deliberately left unenchantable.
 */
public abstract class WandItem extends Item {
    //  The enchanting table refuses to price anything with an enchantability of 0, so wands
    //  need a real number. Higher values buy larger results for less XP.
    private static final int ENCHANTMENT_VALUE = 10;

    public WandItem(Properties properties) {
        super(properties);
    }

    /**
     * The single rule behind every gate below: a wand only takes enchantments while it can
     * break. {@code Properties.durability} is what puts the MAX_DAMAGE component on a stack,
     * so this reads back exactly how each wand was registered in {@link net.bennysmith.simplywands.item.ModItems}.
     */
    private static boolean canTakeEnchantments(ItemStack stack) {
        return stack.has(DataComponents.MAX_DAMAGE);
    }

    //  Gates the enchanting table and ItemStack#isEnchantable.
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return canTakeEnchantments(stack);
    }

    //  Vanilla returns 0, which makes an enchanting table offer nothing at all.
    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return canTakeEnchantments(stack) ? ENCHANTMENT_VALUE : 0;
    }

    //  Gates the anvil. Note this is checked independently of isEnchantable, so both have to
    //  agree or an enchanted book would still slip onto an indestructible wand.
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return canTakeEnchantments(stack);
    }

    //  Decides whether an anvil actually applies an enchantment. Wands appear in none of the
    //  vanilla enchantable/* item tags, so without this the anvil would reject everything; but
    //  an unconditional true would also let through enchantments that do nothing on a wand.
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return canTakeEnchantments(stack) && isAllowedOnWands(stack, enchantment);
    }

    //  Decides which enchantments an enchanting table may offer.
    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return canTakeEnchantments(stack) && isAllowedOnWands(stack, enchantment);
    }

    /**
     * Whether this particular wand may receive the given enchantment.
     *
     * <p>Unbreaking and Mending are shared by every damageable wand. Efficiency is not: only the
     * wands that actually read their Efficiency level and scale with it may take it, so the
     * Illumination, Teleport and Ore-Locator Wands - which have no such scaling - refuse it
     * rather than carry an enchantment that does nothing. A subclass that can act on an extra
     * enchantment should widen this.
     *
     * <p>Both sets are tags, so a pack or server owner can extend them without a code change.
     */
    protected boolean isAllowedOnWands(ItemStack stack, Holder<Enchantment> enchantment) {
        if (enchantment.is(ModTags.Enchantments.ALLOWED_ON_WANDS)) {
            return true;
        }
        if (enchantment.is(Enchantments.EFFICIENCY)) {
            return stack.is(ModTags.Items.EFFICIENCY_CAPABLE_WANDS);
        }
        return false;
    }

    /**
     * The wand's Efficiency level, or 0 when it carries none.
     *
     * <p>Wands sit outside vanilla's digger tags, so nothing inherits Efficiency for them and
     * each wand that cares has to read it back through here.
     *
     * @param level any level in the world the wand is used from, used to reach the enchantment
     *              registry - it is not consulted beyond that.
     */
    public static int efficiency(ItemStack stack, Level level) {
        Holder<Enchantment> efficiency = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.EFFICIENCY);
        return stack.getEnchantmentLevel(efficiency);
    }

    /**
     * A wand's base stat plus a per-Efficiency-level bonus, with a floor of the base value so a
     * zero bonus setting leaves the wand exactly as configured.
     */
    public static int scaledByEfficiency(ItemStack stack, Level level, int base, int bonusPerLevel) {
        return base + efficiency(stack, level) * bonusPerLevel;
    }
}
