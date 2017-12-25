package mod.chiselsandbits.integration.mods;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.IChiseledTileContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleTilesOccusionState implements IChiseledTileContainer
{

	final TileEntityBlockChiseled container;
	final World world;
	final BlockPos pos;

	public LittleTilesOccusionState(
			final World w,
			final BlockPos position,
			final TileEntityBlockChiseled chisledBlockPart )
	{
		world = w;
		pos = position;
		container = chisledBlockPart;
	}

	@Override
	public void sendUpdate()
	{
		ModUtil.sendUpdate( world, pos );
	}

	@Override
	public void saveData()
	{
		world.setBlockState( pos, container.getPreferedBlock() );
		TileEntity te = world.getTileEntity( pos );

		if ( te instanceof TileEntityBlockChiseled )
			( (TileEntityBlockChiseled) te ).copyFrom( container );
	}

	@Override
	public boolean isBlobOccluded(
			final VoxelBlob blob )
	{
		return false;
	}

}
