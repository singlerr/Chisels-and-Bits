package mod.chiselsandbits.integration.mcmultipart;

import java.util.Collection;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.multipart.MultipartOcclusionHelper;
import mod.chiselsandbits.api.BoxType;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.IChiseledTileContainer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class MultipartContainerBuilder implements IChiseledTileContainer
{

	final IMultipartContainer targetContainer;
	final TileEntityBlockChiseled container;
	final World world;
	final BlockPos pos;

	public MultipartContainerBuilder(
			final World w,
			final BlockPos position,
			final TileEntityBlockChiseled chisledBlockPart,
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
		ModUtil.sendUpdate( world, pos );
	}

	@Override
	public void saveData()
	{
		MultipartHelper.addPart( world, pos, MultiPartSlots.BITS, ChiselsAndBits.getBlocks().getChiseledDefaultState(), false );
		MultipartHelper.getPartTile( world, pos, MultiPartSlots.BITS ).ifPresent( stuff ->
		{
			if ( stuff instanceof TileEntityBlockChiseled )
			{
				( (TileEntityBlockChiseled) stuff ).copyFrom( container );
			}
		} );
	}

	@Override
	public boolean isBlobOccluded(
			final VoxelBlob blob )
	{
		final ChiseledBlockPart part = new ChiseledBlockPart( null );
		part.setBlob( blob );

		// get new occlusion...
		final Collection<AxisAlignedBB> selfBoxes = part.getBoxes( BoxType.OCCLUSION );

		return MultipartOcclusionHelper.testContainerBoxIntersection( targetContainer, selfBoxes );
	}

}