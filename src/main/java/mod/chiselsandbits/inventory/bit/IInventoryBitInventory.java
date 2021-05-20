package mod.chiselsandbits.inventory.bit;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class IInventoryBitInventory extends AbstractBitInventory
{

    private final IInventory inventory;

    public IInventoryBitInventory(final IInventory inventory) {this.inventory = inventory;}

    /**
     * Gets a copy of the stack that is in the given slot.
     *
     * @param index The index of the slot to read.
     * @return A copy of the stack in the slot.
     */
    @Override
    protected ItemStack getStackInSlot(final int index)
    {
        return inventory.getStackInSlot(index);
    }

    /**
     * The size of the inventory.
     *
     * @return The size of the inventory.
     */
    @Override
    protected int getInventorySize()
    {
        return inventory.getSizeInventory();
    }

    /**
     * Sets the slot with the given index with the given stack.
     *
     * @param index The index of the slot.
     * @param stack The stack to insert.
     */
    @Override
    protected void setSlotContents(final int index, final ItemStack stack)
    {
        inventory.setInventorySlotContents(index, stack);
    }

    @Override
    public boolean isEmpty()
    {
        return inventory.isEmpty();
    }
}
