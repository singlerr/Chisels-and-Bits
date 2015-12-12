package mod.chiselsandbits.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;

import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;
import mod.chiselsandbits.items.ItemChisel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class ChiselInventory
{

	private final EntityPlayer who;
	private final List<ItemStackSlot> options = new ArrayList<ItemStackSlot>();
	private final HashMap<Integer, List<ItemStackSlot>> actionCache = new HashMap<Integer, List<ItemStackSlot>>();

	public ChiselInventory(
			final EntityPlayer who,
			final BlockPos pos,
			final EnumFacing side )
	{
		this.who = who;
		final ItemStack inHand = who.getCurrentEquippedItem();

		if ( inHand != null && inHand.stackSize > 0 && inHand.getItem() instanceof ItemChisel )
		{
			if ( who.canPlayerEdit( pos, side, inHand ) )
			{
				options.add( new ItemStackSlot( who.inventory, who.inventory.currentItem, inHand, who ) );
			}
		}
		else
		{
			final ArrayListMultimap<Integer, ItemStackSlot> discovered = ArrayListMultimap.create();

			for ( int x = 0; x < who.inventory.getSizeInventory(); x++ )
			{
				final ItemStack is = who.inventory.getStackInSlot( x );

				if ( is == inHand )
				{
					continue;
				}

				if ( !who.canPlayerEdit( pos, side, is ) )
				{
					continue;
				}

				if ( is != null && is.stackSize > 0 && is.getItem() instanceof ItemChisel )
				{
					final ToolMaterial newMat = ( (ItemChisel) is.getItem() ).whatMaterial();
					discovered.put( newMat.getHarvestLevel(), new ItemStackSlot( who.inventory, x, is, who ) );
				}
			}

			final List<ItemStackSlot> allValues = Lists.newArrayList( discovered.values() );
			for ( final ItemStackSlot f : Lists.reverse( allValues ) )
			{
				options.add( f );
			}
		}
	}

	public ItemStackSlot getTool(
			final int BlockID )
	{
		if ( !actionCache.containsKey( BlockID ) )
		{
			actionCache.put( BlockID, new ArrayList<ItemStackSlot>( options ) );
		}

		final List<ItemStackSlot> choices = actionCache.get( BlockID );

		if ( choices.isEmpty() )
		{
			return new ItemStackSlot( null, -1, null, who );
		}

		final ItemStackSlot slot = choices.get( choices.size() - 1 );

		if ( slot.isValid() )
		{
			return slot;
		}
		else
		{
			fail( BlockID );
		}

		return getTool( BlockID );
	}

	public void fail(
			final int BlockID )
	{
		final List<ItemStackSlot> choices = actionCache.get( BlockID );

		if ( !choices.isEmpty() )
		{
			choices.remove( choices.size() - 1 );
		}
	}

	public boolean isValid()
	{
		return !options.isEmpty() || who.capabilities.isCreativeMode;
	}

	public void damage(
			final int blk )
	{
		getTool( blk ).damage( who );
	}

}
