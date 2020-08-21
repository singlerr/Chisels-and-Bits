package mod.chiselsandbits.bitbag;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class TargetedInventory implements IInventory
{

	private IInventory src;

	public TargetedInventory()
	{
		src = null;
	}

	public void setInventory(
			final IInventory a )
	{
		src = a;
	}

	@Override
	public int getSizeInventory()
	{
		return src.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(
			final int index )
	{
		return src.getStackInSlot( index );
	}

	@Override
	public ItemStack decrStackSize(
			final int index,
			final int count )
	{
		return src.decrStackSize( index, count );
	}

	@Override
	public ItemStack removeStackFromSlot(
			final int index )
	{
		return src.removeStackFromSlot( index );
	}

	@Override
	public void setInventorySlotContents(
			final int index,
			final ItemStack stack )
	{
		src.setInventorySlotContents( index, stack );
	}

	@Override
	public int getInventoryStackLimit()
	{
		return src.getInventoryStackLimit();
	}

	@Override
	public void markDirty()
	{
		src.markDirty();
	}

    @Override
    public boolean isUsableByPlayer(final PlayerEntity player)
    {
        return src.isUsableByPlayer( player );
    }

	@Override
	public void openInventory(
			final PlayerEntity player )
	{
		src.openInventory( player );
	}

	@Override
	public void closeInventory(
			final PlayerEntity player )
	{
		src.closeInventory( player );
	}

	@Override
	public boolean isItemValidForSlot(
			final int index,
			final ItemStack stack )
	{
		return src.isItemValidForSlot( index, stack );
	}

	@Override
	public void clear()
	{
		src.clear();
	}

	@Override
	public boolean isEmpty()
	{
		return src.isEmpty();
	}

}
