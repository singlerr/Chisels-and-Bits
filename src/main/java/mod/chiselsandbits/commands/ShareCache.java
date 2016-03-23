package mod.chiselsandbits.commands;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

public class ShareCache extends ChunkCache
{

	BlockPos low;
	BlockPos high;

	int xRange;
	int yRange;
	int zRange;
	int xyRange;

	IBlockState[] states;
	TileEntity[] entities;

	public ShareCache(
			final World world,
			final BlockPos min,
			final BlockPos max,
			final int sub )
	{
		super( world, min, max, sub );

		low = min.subtract( new BlockPos( sub, sub, sub ) );
		high = max.add( new BlockPos( sub, sub, sub ) );

		xRange = high.getX() - low.getX() + 1;
		yRange = high.getY() - low.getY() + 1;
		zRange = high.getZ() - low.getZ() + 1;

		xyRange = xRange * yRange;
		final int total = xyRange * zRange;

		states = new IBlockState[total];
		entities = new TileEntity[total];

		int offset = 0;
		for ( int z = 0; z < zRange; ++z )
		{
			for ( int y = 0; y < yRange; ++y )
			{
				for ( int x = 0; x < xRange; ++x )
				{
					final BlockPos pos = low.add( x, y, z );

					states[offset] = world.getBlockState( pos );
					entities[offset] = world.getTileEntity( pos );
					++offset;
				}
			}
		}
	}

	@Override
	public TileEntity getTileEntity(
			final BlockPos pos )
	{
		return getFromPos( entities, pos );
	}

	@Override
	public int getCombinedLight(
			final BlockPos pos,
			final int lightValue )
	{
		return 0;
	}

	@Override
	public IBlockState getBlockState(
			final BlockPos pos )
	{
		final IBlockState s = getFromPos( states, pos );

		if ( s == null )
		{
			return Blocks.air.getDefaultState();
		}

		return s;
	}

	private <T> T getFromPos(
			final T[] list,
			final BlockPos pos )
	{
		final int xIndex = pos.getX() - low.getX();
		final int yIndex = pos.getY() - low.getY();
		final int zIndex = pos.getZ() - low.getZ();

		if ( xIndex >= 0 && yIndex >= 0 && zIndex >= 0 )
		{
			if ( xIndex < xRange && yIndex < yRange && zIndex < zRange )
			{
				return list[xIndex + yIndex * xRange + zIndex * xyRange];
			}
		}

		return null;
	}

}
