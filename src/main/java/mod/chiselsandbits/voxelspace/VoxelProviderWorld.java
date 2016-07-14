package mod.chiselsandbits.voxelspace;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.chiseledblock.data.IVoxelAccess;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.api.BitAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class VoxelProviderWorld implements IVoxelProvider
{

	final IBlockAccess w;

	public VoxelProviderWorld(
			final IBlockAccess w )
	{
		this.w = w;
	}

	@Override
	public IVoxelAccess get(
			final int x,
			final int y,
			final int z )
	{
		try
		{
			final BitAccess access = (BitAccess) ChiselsAndBits.getApi().getBitAccess( w, new BlockPos( x, y, z ) );
			return new VoxelBlobStateReference( access.getNativeBlob(), 0 );
		}
		catch ( final CannotBeChiseled e )
		{
			return new VoxelBlob();
		}
	}

}
