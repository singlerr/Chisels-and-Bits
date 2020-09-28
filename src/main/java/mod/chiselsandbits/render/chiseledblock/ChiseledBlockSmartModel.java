package mod.chiselsandbits.render.chiseledblock;

import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateInstance;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.client.model.baked.BaseSmartModel;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.ICacheClearable;
import mod.chiselsandbits.render.ModelCombined;
import mod.chiselsandbits.render.NullBakedModel;
import mod.chiselsandbits.render.cache.CacheMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.ForgeConfig;

import java.io.IOException;
import java.util.*;

public class ChiseledBlockSmartModel extends BaseSmartModel implements ICacheClearable
{

    static final CacheMap<VoxelBlobStateReference, ChiseledBlockBaked> solidCache  = new CacheMap<>();
    static final CacheMap<ItemStack, IBakedModel>                      itemToModel = new CacheMap<>();
    static final CacheMap<VoxelBlobStateInstance, Integer>             sideCache   = new CacheMap<>();

    @SuppressWarnings("unchecked")
    static private final Map<ModelRenderState, ChiseledBlockBaked>[] modelCache = new Map[ChiselRenderType.values().length];
    static
    {
        // setup layers.
        for (final ChiselRenderType l : ChiselRenderType.values())
        {
            modelCache[l.ordinal()] = Collections.synchronizedMap(new WeakHashMap<>());
        }
    }
    public static int getSides(
      final TileEntityBlockChiseled te)
    {
        final VoxelBlobStateReference ref = te.getBlobStateReference();
        Integer out;

        if (ref == null)
        {
            return 0;
        }

        synchronized (sideCache)
        {
            out = sideCache.get(ref.getInstance());
            if (out == null)
            {
                final VoxelBlob blob = ref.getVoxelBlob();

                // ignore non-solid, and fluids.
                blob.filter(RenderType.getSolid());
                blob.filterFluids(false);

                out = blob.getSideFlags(0, VoxelBlob.dim_minus_one, VoxelBlob.dim2);
                sideCache.put(ref.getInstance(), out);
            }
        }

        return out;
    }

    public static ChiseledBlockBaked getCachedModel(
      final TileEntityBlockChiseled te,
      final ChiselRenderType layer)
    {
        final VoxelBlobStateReference data = te.getBlobStateReference();
        Integer blockP = te.getPrimaryBlockStateId();
        return getCachedModel(blockP, data, layer, getModelFormat(), Objects.requireNonNull(te.getWorld()).rand);
    }

    private static VertexFormat getModelFormat()
    {
        return DefaultVertexFormats.BLOCK;
    }

    public static boolean ForgePipelineDisabled()
    {
        return !ForgeConfig.CLIENT.forgeLightPipelineEnabled.get() || ChiselsAndBits.getConfig().getServer().disableCustomVertexFormats.get();
    }

    private static ChiseledBlockBaked getCachedModel(
      final Integer blockP,
      final VoxelBlobStateReference data,
      final ChiselRenderType layer,
      final VertexFormat format,
      final Random random)
    {
        if (data == null)
        {
            return new ChiseledBlockBaked(blockP, layer, null, format);
        }

        ChiseledBlockBaked out = null;

        if (format == getModelFormat())
        {
            if (layer == ChiselRenderType.SOLID)
            {
                out = solidCache.get(data);
            }
        }

        if (out == null)
        {
            out = new ChiseledBlockBaked(blockP, layer, data, format);

            if (out.isEmpty())
            {
                out = ChiseledBlockBaked.breakingParticleModel(layer, blockP, random);
            }

            if (format == getModelFormat())
            {
                if (layer == ChiselRenderType.SOLID)
                {
                    solidCache.put(data, out);
                }
            }
        }

        return out;
    }

    @Override
    public IBakedModel handleBlockState(final BlockState state, final Random rand, final IModelData modelData)
    {
        if (state == null)
        {
            return NullBakedModel.instance;
        }

        // This seems silly, but it proves to be faster in practice.
        VoxelBlobStateReference data = modelData.getData(TileEntityBlockChiseled.MP_VBSR);
        Integer blockP = modelData.getData(TileEntityBlockChiseled.MP_PBSI);
        blockP = blockP == null ? 0 : blockP;

        final RenderType layer = net.minecraftforge.client.MinecraftForgeClient.getRenderLayer();

        if (layer == null)
        {
            final ChiseledBlockBaked[] models = new ChiseledBlockBaked[ChiselRenderType.values().length];
            int o = 0;

            for (final ChiselRenderType l : ChiselRenderType.values())
            {
                models[o++] = getCachedModel(blockP, data, l, getModelFormat(), rand);
            }

            return new ModelCombined(models);
        }

        IBakedModel baked;
        if (layer == RenderType.getSolid())
        {
            final ChiseledBlockBaked a = getCachedModel(blockP, data, ChiselRenderType.fromLayer(layer, false), getModelFormat(), rand);
            final ChiseledBlockBaked b = getCachedModel(blockP, data, ChiselRenderType.fromLayer(layer, true), getModelFormat(), rand);

            if (a.isEmpty())
            {
                baked = b;
            }
            else if (b.isEmpty())
            {
                baked = a;
            }
            else
            {
                baked = new ModelCombined(a, b);
            }
        }
        else
        {
            baked = getCachedModel(blockP, data, ChiselRenderType.fromLayer(layer, false), getModelFormat(), rand);
        }

        return baked;
    }

    @Override
    public IBakedModel func_239290_a_(final IBakedModel originalModel, final ItemStack stack, final World world, final LivingEntity entity)
    {
        IBakedModel mdl = itemToModel.get(stack);

        if (mdl != null)
        {
            return mdl;
        }

        CompoundNBT c = stack.getTag();
        if (c == null)
        {
            return this;
        }

        c = c.getCompound(ModUtil.NBT_BLOCKENTITYTAG);

        final byte[] data = c.getByteArray(NBTBlobConverter.NBT_LEGACY_VOXEL);
        byte[] vdata = c.getByteArray(NBTBlobConverter.NBT_VERSIONED_VOXEL);
        final Integer blockP = c.getInt(NBTBlobConverter.NBT_PRIMARY_STATE);

        if (vdata.length == 0 && data.length > 0)
        {
            final VoxelBlob xx = new VoxelBlob();

            try
            {
                xx.fromLegacyByteArray(data);
            }
            catch (final IOException e)
            {
                // :_(
            }

            vdata = xx.blobToBytes(VoxelBlob.VERSION_COMPACT);
        }

        final IBakedModel[] models = new IBakedModel[ChiselRenderType.values().length];
        for (final ChiselRenderType l : ChiselRenderType.values())
        {
            models[l.ordinal()] = getCachedModel(blockP, new VoxelBlobStateReference(vdata, 0L), l, DefaultVertexFormats.BLOCK, world.rand);
        }

        mdl = new ModelCombined(models);

        itemToModel.put(stack, mdl);

        return mdl;
    }

    @Override
    public void clearCache()
    {
        for (final ChiselRenderType l : ChiselRenderType.values())
        {
            modelCache[l.ordinal()].clear();
        }

        sideCache.clear();
        solidCache.clear();
        itemToModel.clear();
    }

    @Override
    public boolean func_230044_c_()
    {
        return true;
    }
}
