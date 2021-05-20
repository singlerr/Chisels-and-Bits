package mod.chiselsandbits.api.inventory.bit;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

/**
 * A bit inventory which can be converted to an itemstack.
 */
public interface IBitInventoryItemStack extends IBitInventory, IInventory
{

    /**
     * Converts this bit inventory into an itemstack.
     *
     * @return The itemstack which represents this inventory.
     */
    ItemStack toItemStack();

    /**
     * This lists the contents of the itemstacks bit inventory.
     *
     * @return The contents.
     */
    List<ITextComponent> listContents();

    /**
     * Calculates the fullness ratio of the itemstack inventory.
     * Useful to render the fullness ration as durability bar on the item.
     *
     * @return The filled ratio.
     */
    double getFilledRatio();

    /**
     * Clears the bit inventory of the current state.
     *
     * @param state The blockstate to remove from the bit inventory.
     */
    void clear(BlockState state);

    /**
     * Sorts the bit inventory.
     */
    void sort();
}
