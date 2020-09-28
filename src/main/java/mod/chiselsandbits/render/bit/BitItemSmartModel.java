package mod.chiselsandbits.render.bit;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.events.TickHandler;
import mod.chiselsandbits.interfaces.ICacheClearable;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.client.model.baked.BaseSmartModel;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.render.ModelCombined;
import mod.chiselsandbits.render.chiseledblock.ChiselRenderType;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBaked;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class BitItemSmartModel extends BaseSmartModel implements ICacheClearable
{
	static private final HashMap<Integer, IBakedModel> modelCache      = new HashMap<Integer, IBakedModel>();
	static private final HashMap<Integer, IBakedModel> largeModelCache = new HashMap<Integer, IBakedModel>();

    static private final NonNullList<ItemStack> alternativeStacks = NonNullList.create();

	private IBakedModel getCachedModel(
			int stateID,
			final boolean large )
	{
	    if (stateID == 0) {
	        //We are running an empty bit, for display purposes.
            //Lets loop:
            if (alternativeStacks.isEmpty())
                ModItems.ITEM_BLOCK_BIT.get().fillItemGroup(Objects.requireNonNull(ModItems.ITEM_BLOCK_BIT.get().getGroup()), alternativeStacks);

            stateID = ItemChiseledBit.getStackState( alternativeStacks.get  ((int) (TickHandler.getClientTicks() % ((alternativeStacks.size() * 20L)) / 20L) + 1));
        }

		final HashMap<Integer, IBakedModel> target = large ? largeModelCache : modelCache;
		IBakedModel out = target.get( stateID );

		if ( out == null )
		{
			if ( large )
			{
				final VoxelBlob blob = new VoxelBlob();
				blob.fill( stateID );
				final VoxelBlobStateReference ref = new VoxelBlobStateReference( blob, 0 );
				final IBakedModel a = new ChiseledBlockBaked( stateID, ChiselRenderType.SOLID, ref,  DefaultVertexFormats.BLOCK );
				final IBakedModel b = new ChiseledBlockBaked( stateID, ChiselRenderType.SOLID_FLUID, ref,  DefaultVertexFormats.BLOCK );
				final IBakedModel c = new ChiseledBlockBaked( stateID, ChiselRenderType.CUTOUT_MIPPED, ref, DefaultVertexFormats.BLOCK );
				final IBakedModel d = new ChiseledBlockBaked( stateID, ChiselRenderType.CUTOUT, ref,  DefaultVertexFormats.BLOCK );
				final IBakedModel e = new ChiseledBlockBaked( stateID, ChiselRenderType.TRANSLUCENT, ref, DefaultVertexFormats.BLOCK );
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

    public IBakedModel func_239290_a_(
      final IBakedModel originalModel,
      final ItemStack stack,
      final World world,
      final LivingEntity entity )
    {
        return getCachedModel( ItemChiseledBit.getStackState( stack ), ClientSide.instance.holdingShift() );
    }

	@Override
	public void clearCache()
	{
		modelCache.clear();
		largeModelCache.clear();
	}

    @Override
    public boolean func_230044_c_()
    {
        return true;
    }
}
