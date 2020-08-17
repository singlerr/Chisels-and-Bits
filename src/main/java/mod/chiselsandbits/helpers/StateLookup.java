package mod.chiselsandbits.helpers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

public class StateLookup
{

	public static class CachedStateLookup extends StateLookup
	{

		private final BlockState[] states;

		public CachedStateLookup()
		{
			final ArrayList<BlockState> list = new ArrayList<>();

			for ( final Block blk : ForgeRegistries.BLOCKS)
			{
				for ( final BlockState state : blk.getStateContainer().getValidStates() )
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

			states = list.toArray( new BlockState[list.size()] );
		}

		@Override
		public BlockState getStateById(
				final int blockStateID )
		{
			return blockStateID >= 0 && blockStateID < states.length ? states[blockStateID] == null ? Blocks.AIR.getDefaultState() : states[blockStateID] : Blocks.AIR.getDefaultState();
		}

	}

	public int getStateId(
			final BlockState state )
	{
		return Block.getStateId( state );
	}

	public BlockState getStateById(
			final int blockStateID )
	{
		return Block.getStateById( blockStateID );
	}

}
