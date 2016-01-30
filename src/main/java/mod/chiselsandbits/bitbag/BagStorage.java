package mod.chiselsandbits.bitbag;

import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;

public class BagStorage implements IItemHandler, INBTSerializable<NBTTagCompound>
{

	public static final int max_size = 63;
	final int[] contents;

	public BagStorage()
	{
		final int len = max_size * ItemBitBag.intsPerBitType;
		contents = new int[len];
	}

	/**
	 * Returns the number of slots available
	 *
	 * @return The number of slots available
	 **/
	@Override
	public int getSlots()
	{
		return max_size;
	}

	/**
	 * Returns the ItemStack in a given slot.
	 *
	 * The result's stack size may be greater than the itemstacks max size.
	 *
	 * If the result is null, then the slot is empty. If the result is not null
	 * but the stack size is zero, then it represents an empty slot that will
	 * only accept* a specific itemstack.
	 *
	 * <p/>
	 * IMPORTANT: This ItemStack MUST NOT be modified. This method is not for
	 * altering an inventories contents. Any implementers who are able to detect
	 * modification through this method should throw an exception.
	 * <p/>
	 * SERIOUSLY: DO NOT MODIFY THE RETURNED ITEMSTACK
	 *
	 * @param slot
	 *            Slot to query
	 * @return ItemStack in given slot. May be null.
	 **/
	@Override
	public ItemStack getStackInSlot(
			final int slot )
	{
		if ( slot < max_size )
		{
			final int qty = contents[ItemBitBag.intsPerBitType * slot + ItemBitBag.offset_qty];
			final int id = contents[ItemBitBag.intsPerBitType * slot + ItemBitBag.offset_state_id];

			if ( id != 0 && qty > 0 )
			{
				return ItemChiseledBit.createStack( id, qty, false );
			}
		}
		return null;
	}

	/**
	 * Inserts an ItemStack into the given slot and return the remainder. Note:
	 * This behavior is subtly different from IFluidHandlers.fill()
	 *
	 * @param slot
	 *            Slot to insert into.
	 * @param stack
	 *            ItemStack to insert
	 * @param simulate
	 *            If true, the insertion is only simulated
	 * @return The remaining ItemStack that was not inserted (if the entire
	 *         stack is accepted, then return null)
	 **/
	@Override
	public ItemStack insertItem(
			final int slot,
			final ItemStack stack,
			final boolean simulate )
	{
		if ( slot >= 0 && slot < max_size && stack != null )
		{
			final int id_index = ItemBitBag.intsPerBitType * slot + ItemBitBag.offset_state_id;
			final int qty_index = ItemBitBag.intsPerBitType * slot + ItemBitBag.offset_qty;

			final int id = contents[id_index];
			final int qty = id == 0 ? 0 : contents[qty_index];

			final ItemType type = ChiselsAndBits.getApi().getItemType( stack );
			if ( type == ItemType.CHISLED_BIT )
			{
				try
				{
					final IBitBrush brush = ChiselsAndBits.getApi().createBrush( stack );
					if ( brush.getStateID() == id || id == 0 )
					{
						int newTotal = qty + stack.stackSize;
						final int overFlow = newTotal > getMaxStackSize() ? newTotal - getMaxStackSize() : 0;
						newTotal -= overFlow;

						if ( !simulate )
						{
							contents[id_index] = brush.getStateID();
							contents[qty_index] = newTotal;
						}

						if ( overFlow > 0 )
						{
							return ItemChiseledBit.createStack( brush.getStateID(), overFlow, false );
						}

						return null;
					}
				}
				catch ( final InvalidBitItem e )
				{
					// something is wrong.
				}
			}
		}

		return stack;
	}

	private int getMaxStackSize()
	{
		return ChiselsAndBits.getConfig().bagStackSize;
	}

	/**
	 * Extracts an ItemStack from the given slot. The returned value must be
	 * null if nothing is extracted, otherwise it's stack size must not be
	 * greater than amount or the itemstacks getMaxStackSize().
	 *
	 * @param slot
	 *            Slot to extract from.
	 * @param amount
	 *            Amount to extract (may be greater than the current stacks max
	 *            limit)
	 * @param simulate
	 *            If true, the extraction is only simulated
	 * @return ItemStack extracted from the slot, must be null, if nothing can
	 *         be extracted
	 **/
	@Override
	public ItemStack extractItem(
			final int slot,
			final int amount,
			final boolean simulate )
	{
		if ( slot >= 0 && slot < max_size )
		{
			final int id_index = ItemBitBag.intsPerBitType * slot + ItemBitBag.offset_state_id;
			final int qty_index = ItemBitBag.intsPerBitType * slot + ItemBitBag.offset_qty;

			final int id = contents[id_index];
			final int qty = id == 0 ? 0 : contents[qty_index];

			final int extracted = qty >= amount ? amount : qty;
			if ( extracted > 0 )
			{
				if ( !simulate )
				{
					contents[qty_index] -= extracted;
					if ( contents[qty_index] <= 0 )
					{
						contents[id_index] = 0;
					}
				}

				return ItemChiseledBit.createStack( id, extracted, false );
			}
		}

		return null;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		final NBTTagCompound out = new NBTTagCompound();
		out.setIntArray( "contents", contents );
		return out;
	}

	@Override
	public void deserializeNBT(
			final NBTTagCompound tag )
	{
		final int[] src = tag.getIntArray( "contents" );
		if ( src != null && src.length > 0 )
		{
			System.arraycopy( src, 0, contents, 0, Math.min( src.length, contents.length ) );
		}
	}

}
