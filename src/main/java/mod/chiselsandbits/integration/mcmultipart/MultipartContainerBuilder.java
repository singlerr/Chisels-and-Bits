package mod.chiselsandbits.integration.mcmultipart;

import mcmultipart.multipart.IMultipartContainer;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.interfaces.IChiseledTileContainer;

class MultipartContainerBuilder implements IChiseledTileContainer
{

	IMultipartContainer targetContainer;
	final ChisledBlockPart container;

	public MultipartContainerBuilder(
			final ChisledBlockPart chisledBlockPart,
			final IMultipartContainer targ )
	{
		container = chisledBlockPart;
		targetContainer = targ;
	}

	@Override
	public void sendUpdate()
	{

	}

	@Override
	public void saveData()
	{
		targetContainer.addPart( container );
		container.getTile();// update container...
	}

	@Override
	public boolean isBlobOccluded(
			final VoxelBlob blob )
	{
		return false;
	}

}
