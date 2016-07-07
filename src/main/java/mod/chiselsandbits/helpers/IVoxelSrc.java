package mod.chiselsandbits.helpers;

import mod.chiselsandbits.chiseledblock.data.BitState;

public interface IVoxelSrc
{

	BitState getSafe(
			int x,
			int y,
			int z );

}
