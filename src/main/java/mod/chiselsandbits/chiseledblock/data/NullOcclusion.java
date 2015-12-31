package mod.chiselsandbits.chiseledblock.data;

import mod.chiselsandbits.interfaces.IBlobOcclusion;

public class NullOcclusion implements IBlobOcclusion
{

	@Override
	public boolean isBlobOccluded(
			final VoxelBlob blob )
	{
		return false;
	}

}
