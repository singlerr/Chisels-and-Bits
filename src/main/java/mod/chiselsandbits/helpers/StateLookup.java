package mod.chiselsandbits.helpers;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class StateLookup
{

	public static class CachedStateLookup extends StateLookup
	{

		private final IBlockState[] states;

		public CachedStateLookup()
		{
			final ArrayList<IBlockState> list = new ArrayList<IBlockState>();

			for ( final Block blk : Block.REGISTRY )
			{
				for ( final IBlockState state : blk.getBlockState().getValidStates() )
				{
					final int id = ModUtil.getStateId( state );

					list.ensureCapacity( id );
					while ( list.size() <= id )
					{
						list.add( null );
					}

					list.set( id, state );
				}
			}

			states = list.toArray( new IBlockState[list.size()] );
		}

		@Override
		public IBlockState getStateById(
				final int blockStateID )
		{
			return blockStateID >= 0 && blockStateID < states.length ? states[blockStateID] == null ? Blocks.AIR.getDefaultState() : states[blockStateID] : Blocks.AIR.getDefaultState();
		}

	}

	public int getStateId(
			final IBlockState state )
	{
		return Block.getStateId( state );
	}

	public IBlockState getStateById(
			final int blockStateID )
	{
		return Block.getStateById( blockStateID );
	}

}
