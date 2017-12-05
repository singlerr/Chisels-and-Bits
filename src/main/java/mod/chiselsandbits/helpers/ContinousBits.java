package mod.chiselsandbits.helpers;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class ContinousBits implements IContinuousInventory
{
	final int stateID;
	private final ActingPlayer who;
	private final List<ItemStackSlot> options = new ArrayList<ItemStackSlot>();
	private final List<BagInventory> bags = new ArrayList<BagInventory>();
	private final InfiniteBitStorage infiniteStorage;
	private final IContinuousInventory chisels;
	private final boolean canEdit;

	ItemStackSlot fakeSlot;

	public ContinousBits(
			final ActingPlayer src,
			final BlockPos pos,
			final int stateID,
			IContinuousInventory chisels,
			InfiniteBitStorage infiniteStorage )
	{
		who = src;
		this.stateID = stateID;
		final IInventory inv = src.getInventory();

		this.chisels = chisels;
		this.infiniteStorage = infiniteStorage;

		// test can edit...
		canEdit = who.canPlayerManipulate( pos, EnumFacing.UP, new ItemStack( ChiselsAndBits.getItems().itemChiselDiamond, 1 ), true );

		ItemStackSlot handSlot = null;

		fakeSlot = new ItemStackSlot( ItemChiseledBit.createStack( stateID, 1, true ) );

		for ( int zz = 0; zz < inv.getSizeInventory(); zz++ )
		{
			final ItemStack which = inv.getStackInSlot( zz );
			if ( which != null && which.getItem() instanceof ItemChiseledBit )
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

			if ( BagInventory.isBag( which ) )
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
		if ( options.isEmpty() )
		{
			return fakeSlot;
		}

		return options.get( 0 );
	}

	@Override
	public boolean hasUses(
			int state,
			int fullSize )
	{
		if ( who.isCreative() )
			return true;

		int total = infiniteStorage.getCount( state );

		for ( ItemStackSlot slot : options )
		{
			total += ModUtil.getStackSize( slot.getStack() );
		}

		for ( final BagInventory bag : bags )
		{
			total += bag.countItems( state );
		}

		return total >= fullSize;
	}

	@Override
	public void useItem(
			final int blk )
	{
		// if possible remove it form here first.
		if ( infiniteStorage.dec( blk ) )
		{
			return;
		}

		final ItemStackSlot slot = options.get( 0 );

		if ( ModUtil.getStackSize( slot.getStack() ) <= 1 )
		{
			for ( final BagInventory bag : bags )
			{
				slot.replaceStack( bag.restockItem( slot.getStack(), slot.getStackType() ) );
			}
		}

		slot.consume();

		if ( slot.isValid() )
		{
			for ( final BagInventory bag : bags )
			{
				slot.replaceStack( bag.restockItem( slot.getStack(), slot.getStackType() ) );
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
		if ( !options.isEmpty() )
			return true;

		int left = infiniteStorage.getCount( stateID );

		if ( left == 0 && infiniteStorage.chiselBlock( stateID, who, chisels ) )
			return true;

		return left > 0;
	}

}
