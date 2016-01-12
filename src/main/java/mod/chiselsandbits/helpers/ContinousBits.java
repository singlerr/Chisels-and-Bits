package mod.chiselsandbits.helpers;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ContinousBits implements IContinuousInventory
{
	final int stateID;
	private final EntityPlayer who;
	private final List<ItemStackSlot> options = new ArrayList<ItemStackSlot>();
	private final List<BagInventory> bags = new ArrayList<BagInventory>();

	public ContinousBits(
			final EntityPlayer src,
			final int stateID )
	{
		who = src;
		this.stateID = stateID;
		final IInventory inv = src.inventory;

		ItemStackSlot handSlot = null;

		for ( int zz = 0; zz < inv.getSizeInventory(); zz++ )
		{
			final ItemStack which = inv.getStackInSlot( zz );
			if ( which != null && which.getItem() instanceof ItemChiseledBit )
			{
				if ( ItemChiseledBit.getStackState( which ) == stateID )
				{
					if ( zz == src.inventory.currentItem )
					{
						handSlot = new ItemStackSlot( inv, zz, which, src );
					}
					else
					{
						options.add( new ItemStackSlot( inv, zz, which, src ) );
					}
				}
			}

			if ( which != null && which.getItem() instanceof ItemBitBag )
			{
				bags.add( new BagInventory( which ) );
			}
		}

		if ( handSlot != null )
		{
			options.add( handSlot );
		}
	}

	@Override
	public ItemStackSlot getItem(
			final int BlockID )
	{
		return options.get( 0 );
	}

	@Override
	public void useItem(
			final int blk )
	{
		final ItemStackSlot slot = options.get( 0 );

		if ( slot.getStack().stackSize <= 1 )
		{
			for ( final BagInventory bag : bags )
			{
				bag.restockItem( slot.getStack() );
			}
		}

		slot.consume();

		if ( slot.isValid() )
		{
			for ( final BagInventory bag : bags )
			{
				bag.restockItem( slot.getStack() );
			}
		}
		else
		{
			options.remove( 0 );
		}
	}

	@Override
	public void fail(
			final int BlockID )
	{
		// hmm.. nope?
	}

	@Override
	public boolean isValid()
	{
		return !options.isEmpty() || who.capabilities.isCreativeMode;
	}

}
