package mod.chiselsandbits.voxelspace;

import mod.chiselsandbits.chiseledblock.data.IVoxelAccess;

public interface IVoxelProvider
{

	IVoxelAccess get(
			int x,
			int y,
			int z );

}
