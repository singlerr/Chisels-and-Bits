package mod.chiselsandbits.integration.mcmultipart;

import java.util.ArrayList;
import java.util.List;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.INormallyOccludingPart;
import mcmultipart.multipart.MultipartHelper;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.interfaces.IChiseledTileContainer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class MultipartContainerBuilder implements IChiseledTileContainer
{

	final IMultipartContainer targetContainer;
	final ChiseledBlockPart container;
	final World world;
	final BlockPos pos;

	public MultipartContainerBuilder(
			final World w,
			final BlockPos position,
			final ChiseledBlockPart chisledBlockPart,
			final IMultipartContainer targ )
	{
		world = w;
		pos = position;
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
		final ChiseledBlockPart part = new ChiseledBlockPart();
		part.getTile().setBlob( blob );

		if ( container.getContainer() == null )
		{
			return false;
		}

		// get new occlusion...
		final List<AxisAlignedBB> selfBoxes = new ArrayList<AxisAlignedBB>();
		part.addOcclusionBoxes( selfBoxes );

		// test occlusion...
		for ( final IMultipart comparePart : container.getContainer().getParts() )
		{
			if ( comparePart instanceof INormallyOccludingPart )
			{
				final List<AxisAlignedBB> partBoxes = new ArrayList<AxisAlignedBB>();
				( (INormallyOccludingPart) comparePart ).addOcclusionBoxes( partBoxes );

				for ( final AxisAlignedBB a : selfBoxes )
				{
					for ( final AxisAlignedBB b : partBoxes )
					{
						if ( a.intersectsWith( b ) )
						{
							return true;
						}
					}
				}
			}
		}

		return false;
	}

}
