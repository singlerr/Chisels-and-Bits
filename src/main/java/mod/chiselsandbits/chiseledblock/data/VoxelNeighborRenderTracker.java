package mod.chiselsandbits.chiseledblock.data;

import java.lang.ref.WeakReference;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.api.BitAccess;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.chiseledblock.ModelRenderState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public final class VoxelNeighborRenderTracker
{
	private WeakReference<VoxelBlobStateReference> lastCenter;
	private ModelRenderState lrs = null;

	private boolean isDynamic;
	private boolean shouldUpdate = false;
	Integer[] faceCount = new Integer[4];

	public VoxelNeighborRenderTracker()
	{
		faceCount = new Integer[BlockRenderLayer.values().length];
	}

	private final ModelRenderState sides = new ModelRenderState( null );

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

		return faces >= ChiselsAndBits.getConfig().dynamicModelFaceCount;
	}

	public void setAbovelimit(
			final BlockRenderLayer layer,
			final int fc )
	{
		faceCount[layer.ordinal()] = fc;
	}

	public boolean isDynamic()
	{
		return isDynamic;
	}

	public void update(
			final boolean isDynamic,
			final IBlockAccess worldObj,
			final BlockPos pos,
			final boolean convertToChiseledBlocks )
	{
		if ( worldObj == null || pos == null )
		{
			return;
		}

		this.isDynamic = isDynamic;

		for ( final EnumFacing f : EnumFacing.VALUES )
		{
			final BlockPos offPos = pos.offset( f );

			final TileEntityBlockChiseled tebc = ModUtil.getChiseledTileEntity( worldObj, offPos, false );
			if ( tebc != null )
			{
				update( f, tebc.getBasicState().getValue( BlockChiseled.UProperty_VoxelBlob ) );
			}
			else if ( convertToChiseledBlocks )
			{
				try
				{
					final BitAccess ba = (BitAccess) ChiselsAndBits.getApi().getBitAccess( worldObj, offPos );
					update( f, new VoxelBlobStateReference( ba.getNativeBlob(), 0 ) );
				}
				catch ( final CannotBeChiseled e )
				{
					update( f, null );
				}
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
		if ( sides.get( f ) == value )
		{
			return;
		}

		synchronized ( this )
		{
			sides.put( f, value );
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

	public void triggerUpdate()
	{
		shouldUpdate = true;
	}

	public boolean isShouldUpdate()
	{
		final boolean res = shouldUpdate;
		shouldUpdate = false;
		return res;
	}

}
