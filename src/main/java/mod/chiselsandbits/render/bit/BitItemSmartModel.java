package mod.chiselsandbits.render.bit;

import java.util.HashMap;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.render.BaseSmartModel;
import mod.chiselsandbits.render.ModelCombined;
import mod.chiselsandbits.render.chiseledblock.ChisledBlockBaked;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartItemModel;

public class BitItemSmartModel extends BaseSmartModel implements ISmartItemModel
{
	private final HashMap<Integer, IFlexibleBakedModel> modelCache = new HashMap<Integer, IFlexibleBakedModel>();
	private final HashMap<Integer, IFlexibleBakedModel> largeModelCache = new HashMap<Integer, IFlexibleBakedModel>();

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
				final IFlexibleBakedModel a = new ChisledBlockBaked( stateID, EnumWorldBlockLayer.SOLID, ref, null, DefaultVertexFormats.ITEM );
				final IFlexibleBakedModel b = new ChisledBlockBaked( stateID, EnumWorldBlockLayer.CUTOUT_MIPPED, ref, null, DefaultVertexFormats.ITEM );
				final IFlexibleBakedModel c = new ChisledBlockBaked( stateID, EnumWorldBlockLayer.CUTOUT, ref, null, DefaultVertexFormats.ITEM );
				final IFlexibleBakedModel d = new ChisledBlockBaked( stateID, EnumWorldBlockLayer.TRANSLUCENT, ref, null, DefaultVertexFormats.ITEM );
				out = new ModelCombined( a, b, c, d );
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
