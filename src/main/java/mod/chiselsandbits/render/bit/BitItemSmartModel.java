package mod.chiselsandbits.render.bit;

import java.util.HashMap;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.render.BaseSmartModel;
import mod.chiselsandbits.render.ModelCombined;
import mod.chiselsandbits.render.chiseledblock.ChiselLayer;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBaked;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartItemModel;

public class BitItemSmartModel extends BaseSmartModel implements ISmartItemModel
{
	static private final HashMap<Integer, IFlexibleBakedModel> modelCache = new HashMap<Integer, IFlexibleBakedModel>();
	static private final HashMap<Integer, IFlexibleBakedModel> largeModelCache = new HashMap<Integer, IFlexibleBakedModel>();

	public static void resetCache()
	{
		modelCache.clear();
		largeModelCache.clear();
	}

	private IBakedModel getCachedModel(
			final int stateID,
			final boolean large )
	{
		final HashMap<Integer, IFlexibleBakedModel> target = large ? largeModelCache : modelCache;
		IFlexibleBakedModel out = target.get( stateID );

		if ( out == null )
		{
			if ( large )
			{
				final VoxelBlob blob = new VoxelBlob();
				blob.fill( stateID );
				final VoxelBlobStateReference ref = new VoxelBlobStateReference( blob, 0 );
				final IFlexibleBakedModel a = new ChiseledBlockBaked( stateID, ChiselLayer.SOLID, ref, null, DefaultVertexFormats.ITEM );
				final IFlexibleBakedModel b = new ChiseledBlockBaked( stateID, ChiselLayer.SOLID_FLUID, ref, null, DefaultVertexFormats.ITEM );
				final IFlexibleBakedModel c = new ChiseledBlockBaked( stateID, ChiselLayer.CUTOUT_MIPPED, ref, null, DefaultVertexFormats.ITEM );
				final IFlexibleBakedModel d = new ChiseledBlockBaked( stateID, ChiselLayer.CUTOUT, ref, null, DefaultVertexFormats.ITEM );
				final IFlexibleBakedModel e = new ChiseledBlockBaked( stateID, ChiselLayer.TRANSLUCENT, ref, null, DefaultVertexFormats.ITEM );
				out = new ModelCombined( a, b, c, d, e );
			}
			else
			{
				out = new BitItemBaked( stateID );
			}

			target.put( stateID, out );
		}

		return out;
	}

	@Override
	public IBakedModel handleItemState(
			final ItemStack stack )
	{
		return getCachedModel( ItemChiseledBit.getStackState( stack ), ClientSide.instance.holdingShift() );
	}
}
