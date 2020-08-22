package mod.chiselsandbits.helpers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class NullInventory implements IInventory
{

	final int size;

	public NullInventory(
			final int size )
	{
		this.size = size;
	}

	@Override
	public int getSizeInventory()
	{
		return size;
	}

	@Override
	public ItemStack getStackInSlot(
			final int index )
	{
		return ModUtil.getEmptyStack();
	}

	@Override
	public ItemStack decrStackSize(
			final int index,
			final int count )
	{
		return ModUtil.getEmptyStack();
	}

	@Override
	public ItemStack removeStackFromSlot(
			final int index )
	{
		return ModUtil.getEmptyStack();
	}

	@Override
	public void setInventorySlotContents(
			final int index,
			final ItemStack stack )
	{

	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty()
	{

	}

    @Override
    public boolean isUsableByPlayer(final PlayerEntity player)
    {
        return false;
    }

	@Override
	public void openInventory(
			final PlayerEntity player )
	{

	}

	@Override
	public void closeInventory(
			final PlayerEntity player )
	{

	}

	@Override
	public boolean isItemValidForSlot(
			final int index,
			final ItemStack stack )
	{
		return false;
	}

	@Override
	public void clear()
	{

	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

}
