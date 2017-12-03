package mod.chiselsandbits.voxelspace;

import net.minecraft.util.math.BlockPos;

public class VoxelOffsetRegion implements IVoxelSrc
{

	final IVoxelSrc inner;
	BlockPos offset;

	public VoxelOffsetRegion(
			final IVoxelSrc src,
			final BlockPos bitOffset )
	{
		inner = src;
		offset = bitOffset;
	}

	@Override
	public int getSafe(
			final int x,
			final int y,
			final int z )
	{
		return inner.getSafe( x + offset.getX(), y + offset.getY(), z + offset.getZ() );
	}

}
