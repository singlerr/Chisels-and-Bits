package mod.chiselsandbits.integration.mcmultipart;

import java.util.ArrayList;
import java.util.List;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IOccludingPart;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.interfaces.IBlobOcclusion;
import net.minecraft.util.AxisAlignedBB;

public class MultipartBlobOcclusion implements IBlobOcclusion
{

	final ChisledBlockPart container;

	public MultipartBlobOcclusion(
			final ChisledBlockPart chisledBlockPart )
	{
		container = chisledBlockPart;
	}

	@Override
	public boolean isBlobOccluded(
			final VoxelBlob blob )
	{
		final ChisledBlockPart part = new ChisledBlockPart();
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
			if ( comparePart instanceof IOccludingPart )
			{
				final List<AxisAlignedBB> partBoxes = new ArrayList<AxisAlignedBB>();
				( (IOccludingPart) comparePart ).addOcclusionBoxes( partBoxes );

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
