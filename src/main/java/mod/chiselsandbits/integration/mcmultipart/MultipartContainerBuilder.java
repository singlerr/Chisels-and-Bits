package mod.chiselsandbits.integration.mcmultipart;

import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.interfaces.IChiseledTileContainer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

class MultipartContainerBuilder implements IChiseledTileContainer
{

	IMultipartContainer targetContainer;
	final ChisledBlockPart container;
	final World world;
	final BlockPos pos;

	public MultipartContainerBuilder(
			final World w,
			final BlockPos pos,
			final ChisledBlockPart chisledBlockPart,
			final IMultipartContainer targ )
	{
		world = w;
		this.pos = pos;
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
		MultipartHelper.addPart( world, pos, container );
		container.getTile();// update container...
	}

	@Override
	public boolean isBlobOccluded(
			final VoxelBlob blob )
	{
		return false;
	}

}
