package mod.chiselsandbits.voxelspace;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Rotate coordinates into a new axis-aligned orientation
 */
public class VoxelTransformedRegion implements IVoxelSrc
{

	final IVoxelSrc inner;
	int x_x, x_y, x_z;
	int y_x, y_y, y_z;
	int z_x, z_y, z_z;
	BlockPos offset;

	public VoxelTransformedRegion(
			final IVoxelSrc src,
			final EnumFacing x,
			final EnumFacing y,
			final EnumFacing z,
			final BlockPos afterOffset )
	{
		inner = src;
		x_x = x.getFrontOffsetX();
		x_y = x.getFrontOffsetY();
		x_z = x.getFrontOffsetZ();
		y_x = y.getFrontOffsetX();
		y_y = y.getFrontOffsetY();
		y_z = y.getFrontOffsetZ();
		z_x = z.getFrontOffsetX();
		z_y = z.getFrontOffsetY();
		z_z = z.getFrontOffsetZ();
		offset = afterOffset;
	}

	@Override
	public int getSafe(
			final int x,
			final int y,
			final int z )
	{
		final int ox = x_x * x + x_y * y + x_z * z + offset.getX();
		final int oy = y_x * x + y_y * y + y_z * z + offset.getY();
		final int oz = z_x * x + z_y * y + z_z * z + offset.getZ();

		return inner.getSafe( ox, oy, oz );
	}

}
