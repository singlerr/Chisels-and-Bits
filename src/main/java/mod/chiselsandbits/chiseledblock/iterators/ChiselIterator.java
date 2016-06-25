package mod.chiselsandbits.chiseledblock.iterators;

import mod.chiselsandbits.chiseledblock.data.IntegerBox;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

public interface ChiselIterator
{

	IntegerBox getVoxelBox(
			VoxelBlob blobAt,
			boolean b );

	AxisAlignedBB getBoundingBox(
			VoxelBlob nULL_BLOB,
			boolean b );

	boolean hasNext();

	EnumFacing side();

	int x();

	int y();

	int z();

}
