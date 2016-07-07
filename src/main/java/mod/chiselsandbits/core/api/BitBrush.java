package mod.chiselsandbits.core.api;

import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.chiseledblock.data.BitState;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public class BitBrush implements IBitBrush
{

	protected BitState state;

	public BitBrush(
			final int blockStateID )
	{
		state = new BitState( null, 0, Block.getStateById( blockStateID ), null );
	}

	public BitBrush(
			final BitState state )
	{
		this.state = state;
	}

	@Override
	public ItemStack getItemStack(
			final int count )
	{
		if ( state.getStateID() == 0 )
		{
			return null;
		}

		return ItemChiseledBit.createStack( state, count, true );
	}

	@Override
	public boolean isAir()
	{
		return state.isEmpty();
	}

	@Override
	public IBlockState getState()
	{
		if ( state.isEmpty() )
		{
			return null;
		}

		return state.getBlockState();
	}

	@Override
	public int getStateID()
	{
		return state.getStateID();
	}

	public IStringSerializable getExtra()
	{
		return state.getExtra();
	}

}
