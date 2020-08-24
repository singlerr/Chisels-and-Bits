package mod.chiselsandbits.helpers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContinousChisels implements IContinuousInventory
{

	private final ActingPlayer who;
	private final List<ItemStackSlot> options = new ArrayList<ItemStackSlot>();
	private final HashMap<Integer, List<ItemStackSlot>> actionCache = new HashMap<Integer, List<ItemStackSlot>>();
	private final boolean canEdit;

	public ContinousChisels(
			final @Nonnull ActingPlayer who,
			final @Nonnull BlockPos pos,
			final @Nonnull Direction side )
	{
		this.who = who;
		final ItemStack inHand = who.getCurrentEquippedItem();
		final IInventory inv = who.getInventory();

		// test can edit...
		canEdit = who.canPlayerManipulate( pos, side, new ItemStack(ModItems.ITEM_CHISEL_DIAMOND.get(), 1 ), false );

		if ( inHand != null && ModUtil.notEmpty( inHand ) && inHand.getItem() instanceof ItemChisel )
		{
			if ( who.canPlayerManipulate( pos, side, inHand, false ) )
			{
				options.add( new ItemStackSlot( inv, who.getCurrentItem(), inHand, who, canEdit ) );
			}
		}
		else
		{
			final ArrayListMultimap<Integer, ItemStackSlot> discovered = ArrayListMultimap.create();

			for ( int x = 0; x < inv.getSizeInventory(); x++ )
			{
				final ItemStack is = inv.getStackInSlot( x );

				if ( is == inHand )
				{
					continue;
				}

				if ( !who.canPlayerManipulate( pos, side, is, false ) )
				{
					continue;
				}

				if ( is != null && ModUtil.notEmpty( is ) && is.getItem() instanceof ItemChisel )
				{
					final IItemTier newMat = ( (ItemChisel) is.getItem() ).getTier();
					discovered.put( newMat.getHarvestLevel(), new ItemStackSlot( inv, x, is, who, canEdit ) );
				}
			}

			final List<ItemStackSlot> allValues = Lists.newArrayList( discovered.values() );
			for ( final ItemStackSlot f : Lists.reverse( allValues ) )
			{
				options.add( f );
			}
		}
	}

	@Override
	public IItemInInventory getItem(
			final int BlockID )
	{
		if ( !actionCache.containsKey( BlockID ) )
		{
			actionCache.put( BlockID, new ArrayList<ItemStackSlot>( options ) );
		}

		final List<ItemStackSlot> choices = actionCache.get( BlockID );

		if ( choices.isEmpty() )
		{
			return new ItemStackSlot( who.getInventory(), -1, ModUtil.getEmptyStack(), who, canEdit );
		}

		final IItemInInventory slot = choices.get( choices.size() - 1 );

		if ( slot.isValid() )
		{
			return slot;
		}
		else
		{
			fail( BlockID );
		}

		return getItem( BlockID );
	}

	@Override
	public void fail(
			final int BlockID )
	{
		final List<ItemStackSlot> choices = actionCache.get( BlockID );

		if ( !choices.isEmpty() )
		{
			choices.remove( choices.size() - 1 );
		}
	}

	@Override
	public boolean isValid()
	{
		return !options.isEmpty() || who.isCreative();
	}

	@Override
	public boolean useItem(
			final int blk )
	{
		getItem( blk ).damage( who );
		return true;
	}

}
