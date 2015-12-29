package mod.chiselsandbits.chiseledblock.data;

import java.lang.ref.WeakReference;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.chiseledblock.ModelRenderState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;

public class VoxelNeighborRenderTracker
{
	private WeakReference<VoxelBlobStateReference> lastCenter;
	private ModelRenderState lrs = null;

	private boolean isDynamic;
	private boolean shouldUpdate;
	Integer[] faceCount = new Integer[4];

	public VoxelNeighborRenderTracker()
	{
		faceCount = new Integer[EnumWorldBlockLayer.values().length];
	}

	private final VoxelBlobStateReference[] sides = new VoxelBlobStateReference[6];

	public boolean isAboveLimit()
	{
		int faces = 0;

		for ( int x = 0; x < faceCount.length; ++x )
		{
			if ( faceCount[x] == null )
			{
				return false;
			}

			faces += faceCount[x];
		}

		return faces >= ChiselsAndBits.instance.config.dynamicModelFaceCount;
	}

	public void setAbovelimit(
			final EnumWorldBlockLayer layer,
			final int fc )
	{
		faceCount[layer.ordinal()] = fc;
	}

	public boolean isShouldUpdate()
	{
		final boolean out = shouldUpdate;
		shouldUpdate = false;
		return out;
	}

	public boolean isDynamic()
	{
		shouldUpdate = true;
		return isDynamic;
	}

	public void update(
			final boolean isDynamic,
			final World worldObj,
			final BlockPos pos )
	{
		if ( worldObj == null || pos == null )
		{
			return;
		}

		this.isDynamic = isDynamic;

		for ( final EnumFacing f : EnumFacing.VALUES )
		{
			final TileEntityBlockChiseled tebc = ModUtil.getChiseledTileEntity( worldObj, pos.offset( f ) );
			if ( tebc != null )
			{
				update( f, tebc.getBasicState().getValue( BlockChiseled.v_prop ) );
			}
			else
			{
				update( f, null );
			}
		}
	}

	private void update(
			final EnumFacing f,
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
