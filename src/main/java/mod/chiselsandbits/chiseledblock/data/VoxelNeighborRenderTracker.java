package mod.chiselsandbits.chiseledblock.data;

import java.lang.ref.WeakReference;

import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.render.chiseledblock.ModelRenderState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class VoxelNeighborRenderTracker
{
	private WeakReference<VoxelBlobStateReference> lastCenter;
	private ModelRenderState lrs = null;

	private boolean isDynamic;
	private final VoxelBlobStateReference[] sides = new VoxelBlobStateReference[6];

	public boolean isDynamic()
	{
		return isDynamic;
	}

	public void update(
			final boolean isDynamic,
			final World worldObj,
			final BlockPos pos )
	{
		this.isDynamic = isDynamic;

		for ( final EnumFacing f : EnumFacing.VALUES )
		{
			final TileEntity te = worldObj.getTileEntity( pos.offset( f ) );
			if ( te instanceof TileEntityBlockChiseled )
			{
				final TileEntityBlockChiseled tebc = (TileEntityBlockChiseled) te;
				update( f, tebc.isDynamicRender(), tebc.getBasicState().getValue( BlockChiseled.v_prop ) );
			}
			else
			{
				update( f, false, null );
			}
		}
	}

	private void update(
			final EnumFacing f,
			final boolean isDynamic,
			final VoxelBlobStateReference value )
	{
		if ( sides[f.ordinal()] == value )
		{
			return;
		}

		synchronized ( this )
		{
			sides[f.ordinal()] = value;
			lrs = null;
		}
	}

	public ModelRenderState getRenderState(
			final VoxelBlobStateReference data )
	{
		if ( lrs == null || lastCenter == null )
		{
			lrs = new ModelRenderState( sides );
			updateCenter( data );
		}
		else if ( lastCenter.get() != data )
		{
			updateCenter( data );
			lrs = new ModelRenderState( sides );
		}

		return lrs;
	}

	private void updateCenter(
			final VoxelBlobStateReference data )
	{
		lastCenter = new WeakReference<VoxelBlobStateReference>( data );
	}

}
