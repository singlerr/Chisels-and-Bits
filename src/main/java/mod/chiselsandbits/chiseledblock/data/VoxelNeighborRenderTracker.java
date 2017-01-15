package mod.chiselsandbits.chiseledblock.data;

import java.lang.ref.WeakReference;

import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.IStateRef;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.chiseledblock.BlockStateRef;
import mod.chiselsandbits.render.chiseledblock.ModelRenderState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.client.FMLClientHandler;

public final class VoxelNeighborRenderTracker
{
	static final int IS_DYNAMIC = 1;
	static final int IS_LOCKED = 2;
	static final int IS_STATIC = 4;

	private WeakReference<VoxelBlobStateReference> lastCenter;
	private ModelRenderState lrs = null;

	private byte isDynamic;
	private boolean shouldUpdate = false;
	Integer[] faceCount = new Integer[4];

	public void unlockDynamic()
	{
		isDynamic = (byte) ( isDynamic & ~IS_LOCKED );
	}

	public VoxelNeighborRenderTracker()
	{
		faceCount = new Integer[BlockRenderLayer.values().length];

		if ( ChiselsAndBits.getConfig().defaultToDynamicRenderer )
		{
			isDynamic = IS_DYNAMIC | IS_LOCKED;
			for ( int x = 0; x < faceCount.length; ++x )
			{
				faceCount[x] = ChiselsAndBits.getConfig().dynamicModelFaceCount + 1;
			}
		}
		else if ( ChiselsAndBits.getConfig().forceDynamicRenderer )
		{
			isDynamic = IS_DYNAMIC | IS_LOCKED;
		}
	}

	private final ModelRenderState sides = new ModelRenderState( null );

	public boolean isAboveLimit()
	{
		if ( FMLClientHandler.instance().hasOptifine() )
		{
			// I simply cannot figure out why the displaylist uploads for the
			// dynamic renderer don't work with optifine, so unless someone else
			// can solve it; I'm just disabling the dynamic renderer pipeline.

			return false;
		}

		if ( ChiselsAndBits.getConfig().forceDynamicRenderer )
		{
			return true;
		}

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
		return ( isDynamic & IS_DYNAMIC ) != 0;
	}

	public void update(
			final boolean isDynamic,
			final IBlockAccess access,
			final BlockPos pos )
	{
		if ( access == null || pos == null )
		{
			return;
		}

		if ( ( this.isDynamic & IS_LOCKED ) == 0 )
		{
			this.isDynamic = (byte) ( isDynamic ? IS_DYNAMIC : IS_STATIC );
		}

		for ( final EnumFacing f : EnumFacing.VALUES )
		{
			final BlockPos oPos = pos.offset( f );

			final TileEntityBlockChiseled tebc = ModUtil.getChiseledTileEntity( access, oPos );
			assert f != null;
			if ( tebc != null )
			{
				update( f, tebc.getBasicState().getValue( BlockChiseled.UProperty_VoxelBlob ) );
			}
			else
			{
				final int stateid = ModUtil.getStateId( access.getBlockState( oPos ) );

				if ( stateid == 0 )
				{
					update( f, null );
				}
				else
				{
					update( f, new BlockStateRef( stateid ) );
				}
			}
		}
	}

	private void update(
			final EnumFacing f,
			final IStateRef value )
	{
		if ( sameValue( sides.get( f ), value ) )
		{
			return;
		}

		synchronized ( this )
		{
			sides.put( f, value );
			lrs = null;
			triggerUpdate();
		}
	}

	private boolean sameValue(
			final IStateRef iStateRef,
			final IStateRef value )
	{
		if ( iStateRef == value )
		{
			return true;
		}

		if ( iStateRef == null || value == null )
		{
			return false;
		}

		return value.equals( iStateRef );
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
