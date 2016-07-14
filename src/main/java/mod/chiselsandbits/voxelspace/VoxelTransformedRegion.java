package mod.chiselsandbits.voxelspace;

import net.minecraft.util.EnumFacing;

/**
 * Rotate coordinates into a new axis-aligned orientation
 */
public class VoxelTransformedRegion implements IVoxelSrc
{

	final IVoxelSrc inner;
	int x_x, x_y, x_z;
	int y_x, y_y, y_z;
	int z_x, z_y, z_z;

	public VoxelTransformedRegion(
			final IVoxelSrc src,
			final EnumFacing x,
			final EnumFacing y,
			final EnumFacing z )
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
	}

	@Override
	public int getSafe(
			final int x,
			final int y,
			final int z )
	{
		final int ox = x_x * x + y_x * y + z_x * z;
		final int oy = x_y * x + y_y * y + z_y * z;
		final int oz = x_z * x + y_z * y + z_z * z;
		return inner.getSafe( ox, oy, oz );
	}

}
