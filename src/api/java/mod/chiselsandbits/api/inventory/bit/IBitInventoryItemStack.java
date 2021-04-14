package mod.chiselsandbits.api.inventory.bit;

import net.minecraft.item.ItemStack;

/**
 * A bit inventory which can be converted to an itemstack.
 */
public interface IBitInventoryItemStack extends IBitInventory
{

    /**
     * Converts this bit inventory into an itemstack.
     *
     * @return The itemstack which represents this inventory.
     */
    ItemStack toItemStack();
}
