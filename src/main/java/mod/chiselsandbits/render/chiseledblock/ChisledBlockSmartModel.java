package mod.chiselsandbits.render.chiseledblock;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import mcmultipart.client.multipart.ISmartMultipartModel;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.chiseledblock.data.VoxelNeighborRenderTracker;
import mod.chiselsandbits.render.BaseSmartModel;
import mod.chiselsandbits.render.ModelCombined;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;

@Optional.InterfaceList( { @Interface( iface = "mcmultipart.client.multipart.ISmartMultipartModel", modid = "mcmultipart" ) })
public class ChisledBlockSmartModel extends BaseSmartModel implements ISmartItemModel, ISmartBlockModel, ISmartMultipartModel
{
	@SuppressWarnings( "unchecked" )
	static private final Map<ModelRenderState, ChisledBlockBaked>[] modelCache = new Map[4];
	static private final Map<VoxelBlobStateReference, ChisledBlockBaked> solidCache = Collections.synchronizedMap( new WeakHashMap<VoxelBlobStateReference, ChisledBlockBaked>() );
	static private final Map<ItemStack, IBakedModel> itemToModel = Collections.synchronizedMap( new WeakHashMap<ItemStack, IBakedModel>() );
	static private final Map<VoxelBlobStateReference, Integer> sideCache = new WeakHashMap<VoxelBlobStateReference, Integer>();

	static public void resetCache()
	{
		for ( final EnumWorldBlockLayer l : EnumWorldBlockLayer.values() )
		{
			modelCache[l.ordinal()].clear();
		}

		solidCache.clear();
		itemToModel.clear();
	}

	static
	{
		final int count = EnumWorldBlockLayer.values().length;

		if ( modelCache.length != count )
		{
			throw new RuntimeException( "Invalid Number of EnumWorldBlockLayer" );
		}

		// setup layers.
		for ( final EnumWorldBlockLayer l : EnumWorldBlockLayer.values() )
		{
			modelCache[l.ordinal()] = Collections.synchronizedMap( new WeakHashMap<ModelRenderState, ChisledBlockBaked>() );
		}
	}

	public static int getSides(
			final TileEntityBlockChiseled te )
	{
		final VoxelBlobStateReference ref = te.getBlobStateReference();
		Integer out = null;

		if ( ref == null )
		{
			return 0;
		}

		synchronized ( sideCache )
		{
			out = sideCache.get( ref );
			if ( out == null )
			{
				final VoxelBlob blob = ref.getVoxelBlob();
				blob.filter( EnumWorldBlockLayer.SOLID );
				out = blob.getSideFlags( 0, VoxelBlob.dim_minus_one, VoxelBlob.dim2 );
				sideCache.put( ref, out );
			}
		}

		return out;
	}

	public static ChisledBlockBaked getCachedModel(
			final TileEntityBlockChiseled te,
			final EnumWorldBlockLayer layer )
	{
		final IExtendedBlockState myState = te.getBasicState();

		final VoxelBlobStateReference data = myState.getValue( BlockChiseled.v_prop );
		final VoxelNeighborRenderTracker rTracker = myState.getValue( BlockChiseled.n_prop );
		Integer blockP = myState.getValue( BlockChiseled.block_prop );

		blockP = blockP == null ? 0 : blockP;

		return getCachedModel( blockP, data, getRenderState( rTracker, data ), layer, ChiselsAndBitsBakedQuad.VERTEX_FORMAT );
	}

	private static ChisledBlockBaked getCachedModel(
			final Integer blockP,
			final VoxelBlobStateReference data,
			final ModelRenderState mrs,
			final EnumWorldBlockLayer layer,
			final VertexFormat format )
	{
		if ( data == null )
		{
			return new ChisledBlockBaked( blockP, layer, data, new ModelRenderState( null ), format );
		}

		ChisledBlockBaked out = null;

		if ( format == ChiselsAndBitsBakedQuad.VERTEX_FORMAT )
		{
			if ( layer == EnumWorldBlockLayer.SOLID )
			{
				out = solidCache.get( data );
			}
			else
			{
				out = mrs == null ? null : modelCache[layer.ordinal()].get( mrs );
			}
		}

		if ( out == null )
		{
			out = new ChisledBlockBaked( blockP, layer, data, mrs, format );

			if ( out.isEmpty() )
			{
				out = ChisledBlockBaked.breakingParticleModel( layer, blockP );
			}

			if ( format == ChiselsAndBitsBakedQuad.VERTEX_FORMAT )
			{
				if ( layer == EnumWorldBlockLayer.SOLID )
				{
					solidCache.put( data, out );
				}
				else if ( mrs != null )
				{
					modelCache[layer.ordinal()].put( mrs, out );
				}
			}
		}

		return out;
	}

	@Override
	public IBakedModel handlePartState(
			final IBlockState state )
	{
		return handleBlockState( state );
	}

	@Override
	public IBakedModel handleBlockState(
			final IBlockState state )
	{
		final IExtendedBlockState myState = (IExtendedBlockState) state;

		final VoxelBlobStateReference data = myState.getValue( BlockChiseled.v_prop );
		final VoxelNeighborRenderTracker rTracker = myState.getValue( BlockChiseled.n_prop );
		Integer blockP = myState.getValue( BlockChiseled.block_prop );

		blockP = blockP == null ? 0 : blockP;

		final EnumWorldBlockLayer layer = net.minecraftforge.client.MinecraftForgeClient.getRenderLayer();

		if ( rTracker != null && rTracker.isDynamic() )
		{
			return ChisledBlockBaked.breakingParticleModel( layer, blockP );
		}

		final ChisledBlockBaked baked = getCachedModel( blockP, data, getRenderState( rTracker, data ), layer, ChiselsAndBitsBakedQuad.VERTEX_FORMAT );

		if ( rTracker != null )
		{
			rTracker.setAbovelimit( layer, baked.faceCount() );
		}

		return baked;
	}

	private static ModelRenderState getRenderState(
			final VoxelNeighborRenderTracker renderTracker,
			final VoxelBlobStateReference data )
	{
		if ( renderTracker != null )
		{
			return renderTracker.getRenderState( data );
		}

		return null;
	}

	@Override
	public IBakedModel handleItemState(
			final ItemStack stack )
	{
		IBakedModel mdl;
		mdl = itemToModel.get( stack );

		if ( mdl != null )
		{
			return mdl;
		}

		NBTTagCompound c = stack.getTagCompound();
		if ( c == null )
		{
			return this;
		}

		c = c.getCompoundTag( "BlockEntityTag" );
		if ( c == null )
		{
			return this;
		}

		final byte[] data = c.getByteArray( "v" );
		final Integer blockP = c.getInteger( "b" );

		final IFlexibleBakedModel[] models = new IFlexibleBakedModel[EnumWorldBlockLayer.values().length];
		for ( final EnumWorldBlockLayer l : EnumWorldBlockLayer.values() )
		{
			models[l.ordinal()] = getCachedModel( blockP, new VoxelBlobStateReference( data, 0L ), null, l, DefaultVertexFormats.ITEM );
		}

		mdl = new ModelCombined( models );

		itemToModel.put( stack, mdl );

		return mdl;
	}

}
