package mod.chiselsandbits.bitbag;

import java.util.Arrays;

import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.IBitBag;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.item.ItemStack;

public class BagStorage implements IBitBag
{

	protected ItemStack stack;

	public static final int max_size = 63;
	int[] contents;

	protected void setStorage(
			final int[] source )
	{
		contents = source;
	}

	public void onChange()
	{
	}

	@Override
	public boolean equals(
			final Object obj )
	{
		if ( obj instanceof BagStorage )
		{
			return Arrays.equals( contents, ( (BagStorage) obj ).contents );
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( contents );
	}

	@Override
	public int getSlots()
	{
		return max_size;
	}

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
						final int overFlow = newTotal > getBitbagStackSize() ? newTotal - getBitbagStackSize() : 0;
						newTotal -= overFlow;

						if ( !simulate )
						{
							contents[id_index] = brush.getStateID();
							contents[qty_index] = newTotal;

							onChange();
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

	@Override
	public int getBitbagStackSize()
	{
		return ChiselsAndBits.getConfig().bagStackSize;
	}

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

					onChange();
				}

				return ItemChiseledBit.createStack( id, extracted, false );
			}
		}

		return null;
	}

	@Override
	public int getSlotsUsed()
	{
		int used = 0;
		for ( int index = 0; index < contents.length; index += ItemBitBag.intsPerBitType )
		{
			final int qty = contents[index + ItemBitBag.offset_qty];
			final int id = contents[index + ItemBitBag.offset_state_id];

			if ( qty > 0 && id > 0 )
			{
				used++;
			}
		}

		return used;
	}

}
