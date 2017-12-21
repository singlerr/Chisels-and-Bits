package mod.chiselsandbits.helpers;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ContinousBits implements IContinuousInventory
{
	final int stateID;
	private final ActingPlayer who;
	private final List<IItemInInventory> options = new ArrayList<IItemInInventory>();
	private final List<BagInventory> bags = new ArrayList<BagInventory>();
	private final boolean canEdit;

	public ContinousBits(
			final ActingPlayer src,
			final BlockPos pos,
			final int stateID )
	{
		who = src;
		this.stateID = stateID;
		final IInventory inv = src.getInventory();

		// test can edit...
		canEdit = who.canPlayerManipulate( pos, EnumFacing.UP, new ItemStack( ChiselsAndBits.getItems().itemChiselDiamond, 1 ), true );

		ItemStackSlot handSlot = null;

		for ( int zz = 0; zz < inv.getSizeInventory(); zz++ )
		{
			final ItemStack which = inv.getStackInSlot( zz );
			if ( which != null && which.getItem() != null )
			{
				Item i = which.getItem();
				if ( i instanceof ItemChiseledBit )
				{
					if ( ItemChiseledBit.getStackState( which ) == stateID )
					{
						if ( zz == src.getCurrentItem() )
						{
							handSlot = new ItemStackSlot( inv, zz, which, src, canEdit );
						}
						else
						{
							options.add( new ItemStackSlot( inv, zz, which, src, canEdit ) );
						}
					}
				}

				else if ( i instanceof ItemBitBag )
				{
					bags.add( new BagInventory( which ) );
				}

				else if ( which.hasCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null ) )
				{
					IItemHandler internal = which.getCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null );
					for ( int x = 0; x < internal.getSlots(); x++ )
					{
						ItemStack is = internal.getStackInSlot( x );

						if ( is.getItem() instanceof ItemChiseledBit )
						{
							if ( ItemChiseledBit.getStackState( is ) == stateID )
							{
								options.add( new IItemHandlerSlot( internal, x, is, src, canEdit ) );
							}
						}
					}
				}
			}
		}

		if ( handSlot != null )
		{
			options.add( handSlot );
		}
	}

	@Override
	public IItemInInventory getItem(
			final int BlockID )
	{
		return options.get( 0 );
	}

	@Override
	public boolean useItem(
			final int blk )
	{
		final IItemInInventory slot = options.get( 0 );

		if ( slot instanceof ItemStackSlot && ModUtil.getStackSize( slot.getStack() ) <= 1 )
		{
			for ( final BagInventory bag : bags )
			{
				( (ItemStackSlot) slot ).replaceStack( bag.restockItem( slot.getStack(), slot.getStackType() ) );
			}
		}

		boolean worked = slot.consume();

		if ( slot.isValid() )
		{
			if ( slot instanceof ItemStackSlot )
			{
				for ( final BagInventory bag : bags )
				{
					( (ItemStackSlot) slot ).replaceStack( bag.restockItem( slot.getStack(), slot.getStackType() ) );
				}
			}
		}
		else
		{
			options.remove( 0 );
		}

		return worked;
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
		return !options.isEmpty();
	}

}
