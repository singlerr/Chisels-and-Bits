package mod.chiselsandbits.helpers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.Arrays;

public class PlayerCopiedInventory implements IInventory
{

    PlayerInventory logicBase;
	ItemStack[] slots;

	public PlayerCopiedInventory(
			final PlayerInventory original )
	{
		logicBase = original;
		slots = new ItemStack[original.getSizeInventory()];

		for ( int x = 0; x < slots.length; ++x )
		{
			slots[x] = original.getStackInSlot( x );

			if ( slots[x] != null )
			{
				slots[x] = slots[x].copy();
			}
		}
	}

    @Override
    public boolean isEmpty()
    {
        return Arrays.stream(slots).allMatch(ItemStack::isEmpty);
    }

    @Override
    public boolean isUsableByPlayer(final PlayerEntity player)
    {
        return true;
    }

    @Override
	public int getSizeInventory()
	{
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(
			final int index )
	{
		return slots[index];
	}

	@Override
	public ItemStack decrStackSize(
			final int index,
			final int count )
	{
		if ( slots[index] != null )
		{
			if ( ModUtil.getStackSize( slots[index] ) <= count )
			{
				return removeStackFromSlot( index );
			}
			else
			{
				return slots[index].split( count );
			}
		}

		return ModUtil.getEmptyStack();
	}

	@Override
	public ItemStack removeStackFromSlot(
			final int index )
	{
		final ItemStack r = slots[index];
		slots[index] = ModUtil.getEmptyStack();
		return r;
	}

	@Override
	public void setInventorySlotContents(
			final int index,
			final ItemStack stack )
	{
		slots[index] = stack;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return logicBase.getInventoryStackLimit();
	}

	@Override
	public void markDirty()
	{
	}

	@Override
	public boolean isItemValidForSlot(
			final int index,
			final ItemStack stack )
	{
		return logicBase.isItemValidForSlot( index, stack );
	}
	@Override
	public void clear()
	{
		for ( int x = 0; x < slots.length; ++x )
		{
			slots[x] = ModUtil.getEmptyStack();
		}
	}
}
