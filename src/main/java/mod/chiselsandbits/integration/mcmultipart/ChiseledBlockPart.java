package mod.chiselsandbits.integration.mcmultipart;

import java.util.Collection;

import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartOcclusionHelper;
import mod.chiselsandbits.chiseledblock.BoxType;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class ChiseledBlockPart extends TileEntityBlockChiseled implements IMultipartTile
{

	public ChiseledBlockPart()
	{
		// required for loading.
	}

	@Override
	public boolean isBlobOccluded(
			final VoxelBlob blob )
	{
		final ChiseledBlockPart part = new ChiseledBlockPart( null );
		part.setBlob( blob );

		// get new occlusion...
		final Collection<AxisAlignedBB> selfBoxes = part.getBoxes( BoxType.OCCLUSION );

		return MultipartOcclusionHelper.testContainerBoxIntersection( getWorld(), getPos(), selfBoxes );
	}

	@Override
	protected boolean supportsSwapping()
	{
		return false;
	}

	public ChiseledBlockPart(
			final TileEntity tileEntity )
	{
		if ( tileEntity != null )
		{
			copyFrom( (TileEntityBlockChiseled) tileEntity );
		}
	}

	@Override
	public TileEntity getTileEntity()
	{
		return this;
	}

}
