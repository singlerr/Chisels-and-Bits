
package mod.chiselsandbits.bitbag;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BagInventory implements IInventory
{

	final public static int max_size = 63;

	final int[] slots;
	final ItemStack[] stackSlots;

	final ItemStack target;

	static public int getSlotsUsed(
			final ItemStack target )
	{
		final NBTTagCompound tag = target.getTagCompound();

		if ( tag == null )
			return 0;

		if ( !tag.hasKey( "contents" ) )
			return 0;

		final int slots[] = tag.getIntArray( "contents" );

		int used = 0;
		for ( int index = 0; index < slots.length; index += ItemBitBag.intsPerBitType )
		{
			final int qty = slots[index + ItemBitBag.offset_qty];
			final int id = slots[index + ItemBitBag.offset_state_id];

			if ( qty > 0 && id > 0 )
			{
				used++;
			}
		}

		return used;
	}

	public BagInventory(
			final ItemStack is )
	{
		target = is;
		NBTTagCompound tag = target.getTagCompound();

		if ( tag == null )
		{
			is.setTagCompound( tag = new NBTTagCompound() );
		}

		final int slotCount = max_size;
		final int len = slotCount * ItemBitBag.intsPerBitType;

		stackSlots = new ItemStack[slotCount];

		if ( !tag.hasKey( "contents" ) )
		{
			tag.setIntArray( "contents", new int[len] );
		}

		if ( tag.getIntArray( "contents" ).length != len )
		{
			tag.setIntArray( "contents", new int[len] );
		}

		slots = tag.getIntArray( "contents" );
	}

	@Override
	public String getCommandSenderName()
	{
		return null;
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public IChatComponent getDisplayName()
	{
		return null;
	}

	@Override
	public int getSizeInventory()
	{
		return stackSlots.length;
	}

	@Override
	public ItemStack getStackInSlot(
			final int index )
	{
		final int qty = slots[ItemBitBag.intsPerBitType * index + ItemBitBag.offset_qty];
		final int id = slots[ItemBitBag.intsPerBitType * index + ItemBitBag.offset_state_id];

		if ( stackSlots[index] != null )
		{
			stackSlots[index].stackSize = qty;
			return stackSlots[index];
		}

		if ( qty == 0 || id == 0 )
			return null;

		return stackSlots[index] = ItemChiseledBit.createStack( id, qty, false );
	}

	@Override
	public ItemStack decrStackSize(
			final int index,
			int count )
	{
		final int qty = slots[ItemBitBag.intsPerBitType * index + ItemBitBag.offset_qty];
		final int id = slots[ItemBitBag.intsPerBitType * index + ItemBitBag.offset_state_id];

		if ( qty == 0 || id == 0 )
			return null;

		if ( count > qty )
		{
			count = qty;
		}

		slots[ItemBitBag.intsPerBitType * index + ItemBitBag.offset_qty] -= count;

		if ( stackSlots[index] != null )
		{
			stackSlots[index].stackSize -= count;
		}

		return ItemChiseledBit.createStack( id, count, false );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(
			final int index )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(
			final int index,
			final ItemStack stack )
	{
		stackSlots[index] = null;

		if ( stack != null && stack.getItem() instanceof ItemChiseledBit )
		{
			slots[ItemBitBag.intsPerBitType * index + ItemBitBag.offset_qty] = stack.stackSize;
			slots[ItemBitBag.intsPerBitType * index + ItemBitBag.offset_state_id] = ItemChisel.getStackState( stack );
		}
		else
		{
			slots[ItemBitBag.intsPerBitType * index + ItemBitBag.offset_qty] = 0;
			slots[ItemBitBag.intsPerBitType * index + ItemBitBag.offset_state_id] = 0;
		}
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty()
	{
		for ( int x = 0; x < getSizeInventory(); x++ )
			if ( stackSlots[x] != null )
			{
				slots[ItemBitBag.intsPerBitType * x + ItemBitBag.offset_qty] = stackSlots[x].stackSize;
				stackSlots[x] = null;
			}
	}

	@Override
	public boolean isUseableByPlayer(
			final EntityPlayer player )
	{
		return true;
	}

	@Override
	public void openInventory(
			final EntityPlayer player )
	{}

	@Override
	public void closeInventory(
			final EntityPlayer player )
	{}

	@Override
	public boolean isItemValidForSlot(
			final int index,
			final ItemStack stack )
	{
		return stack != null && stack.getItem() instanceof ItemChiseledBit;
	}

	@Override
	public int getField(
			final int id )
	{
		return 0;
	}

	@Override
	public void setField(
			final int id,
			final int value )
	{

	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		for ( int x = 0; x < slots.length; x++ )
		{
			slots[x] = 0;
			stackSlots[x] = null;
		}
	}

	public void restockItem(
			final ItemStack target )
	{
		for ( int x = 0; x < getSizeInventory(); x++ )
		{
			final ItemStack is = getStackInSlot( x );
			if ( is != null && is.getItem() == target.getItem() && ItemChiseledBit.sameBit( target, ItemChisel.getStackState( is ) ) )
			{
				target.stackSize += is.stackSize;
				final int total = target.stackSize;
				target.stackSize = Math.min( is.getMaxStackSize(), target.stackSize );
				final int overage = total - target.stackSize;

				if ( overage > 0 )
				{
					is.stackSize = overage;
				}
				else
				{
					setInventorySlotContents( x, null );
				}

				markDirty();
			}
		}
	}

	public ItemStack insertItem(
			final ItemStack which )
	{
		for ( int x = 0; x < getSizeInventory(); x++ )
		{
			final ItemStack is = getStackInSlot( x );
			if ( is != null && ItemChisel.getStackState( which ) == ItemChisel.getStackState( is ) )
			{
				is.stackSize += which.stackSize;
				final int total = is.stackSize;
				is.stackSize = Math.min( is.getMaxStackSize(), is.stackSize );
				final int overage = total - is.stackSize;
				if ( overage > 0 )
				{
					which.stackSize = overage;
					markDirty();
				}
				else
				{
					markDirty();
					return null;
				}
			}
			else if ( is == null )
			{
				setInventorySlotContents( x, which );
				markDirty();
				return null;
			}
		}

		return which;
	}

	public int extractBit(
			final int bitMeta,
			int total )
	{
		int used = 0;

		for ( int index = 0; index < stackSlots.length; index++ )
		{
			final int qty_idx = ItemBitBag.intsPerBitType * index + ItemBitBag.offset_qty;

			final int qty = slots[qty_idx];
			final int id = slots[ItemBitBag.intsPerBitType * index + ItemBitBag.offset_state_id];

			if ( id == bitMeta && qty > 0 )
			{

				slots[qty_idx] -= total;

				if ( slots[qty_idx] < 0 )
				{
					slots[qty_idx] = 0;
				}

				final int diff = qty - slots[qty_idx];
				used += diff;
				total -= diff;

				if ( 0 == total )
					return used;
			}
		}

		return used;
	}

	@SideOnly( Side.CLIENT )
	public void listContents(
			final List<String> details )
	{
		final HashMap<String, Integer> contents = new HashMap<String, Integer>();

		for ( int x = 0; x < getSizeInventory(); x++ )
		{
			final ItemStack is = getStackInSlot( x );

			if ( is != null )
			{
				final IBlockState state = Block.getStateById( ItemChisel.getStackState( is ) );
				if ( state == null )
				{
					continue;
				}

				final Block blk = state.getBlock();
				if ( blk == null )
				{
					continue;
				}

				final Item what = Item.getItemFromBlock( blk );
				if ( what == null )
				{
					continue;
				}

				final String name = what.getItemStackDisplayName( new ItemStack( what, 1, blk.getMetaFromState( state ) ) );

				Integer count = contents.get( name );
				if ( count == null )
				{
					count = is.stackSize;
				}
				else
				{
					count += is.stackSize;
				}

				contents.put( name, count );
			}
		}

		if ( contents.isEmpty() )
		{
			details.add( LocalStrings.Empty.getLocal() );
		}

		for ( final Entry<String, Integer> e : contents.entrySet() )
		{
			details.add( new StringBuilder().append( e.getValue() ).append( ' ' ).append( e.getKey() ).toString() );
		}
	}

}
