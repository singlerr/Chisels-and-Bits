package mod.chiselsandbits.client.model.baked.chiseled.face;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.DeprecationHelper;
import mod.chiselsandbits.client.model.baked.chiseled.face.model.ModelQuadLayer;
import mod.chiselsandbits.client.model.baked.chiseled.face.model.ModelVertexRange;
import mod.chiselsandbits.client.model.baked.simple.SimpleGeneratedModel;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public final class FaceManager
{

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Random RANDOM = new Random();

    private static final FaceManager INSTANCE = new FaceManager();
    private final Cache<Triple<BlockState, RenderType, Direction>, ModelQuadLayer[]> cache = CacheBuilder.newBuilder()
                                                                                                .expireAfterAccess(1, TimeUnit.MINUTES)
                                                                                                .build();

    private final Cache<BlockState, Integer> colorCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

    private FaceManager()
    {
    }

    public static FaceManager getInstance()
    {
        return INSTANCE;
    }

    public ModelQuadLayer[] getCachedFace(
      final BlockState state,
      final Direction face,
      final RenderType layer)
    {
        if (layer == null)
        {
            return null;
        }

        final Triple<BlockState, RenderType, Direction> key = Triple.of(state, layer, face);

        try
        {
            return cache.get(key, () -> {
                final RenderType original = net.minecraftforge.client.MinecraftForgeClient.getRenderLayer();
                try
                {
                    ForgeHooksClient.setRenderLayer(layer);
                    return buildFaceQuadLayers(state, face);
                }
                finally
                {
                    // restore previous layer.
                    ForgeHooksClient.setRenderLayer(original);
                }
            });
        }
        catch (ExecutionException e)
        {
            LOGGER.error("Failed to determine face cache entry.", e);
            return new ModelQuadLayer[0];
        }
    }

    private ModelQuadLayer[] buildFaceQuadLayers(
      final BlockState state,
      final Direction face)
    {
        final IBakedModel model = solveModel(state, Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state));
        final int lv = Configuration.getInstance().getClient().useGetLightValue.get() ? DeprecationHelper.getLightValue(state) : 0;

        final Fluid fluid = state.getFluidState().getFluid();
        if (fluid != Fluids.EMPTY)
        {
            final ModelQuadLayer[] mp = new ModelQuadLayer[1];
            mp[0] = new ModelQuadLayer();
            mp[0].setColor(fluid.getAttributes().getColor());
            mp[0].setLight(lv);

            final float V = 0.5f;
            final float Uf = 1.0f;
            final float U = 0.5f;
            final float Vf = 1.0f;

            if (face.getAxis() == Direction.Axis.Y)
            {
                mp[0].setSprite(Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fluid.getAttributes().getStillTexture()));
                mp[0].setUvs(new float[] {Uf, Vf, 0, Vf, Uf, 0, 0, 0});
            }
            else if (face.getAxis() == Direction.Axis.X)
            {
                mp[0].setSprite(Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fluid.getAttributes().getFlowingTexture()));
                mp[0].setUvs(new float[] {U, 0, U, V, 0, 0, 0, V});
            }
            else
            {
                mp[0].setSprite(Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fluid.getAttributes().getFlowingTexture()));
                mp[0].setUvs(new float[] {U, 0, 0, 0, U, V, 0, V});
            }

            mp[0].setTint(0);
            return mp;
        }

        final List<ModelQuadLayer.ModelQuadLayerBuilder> layers = Lists.newArrayList();
        final int color = getColorFor(state);

        if (model != null)
        {
            final List<BakedQuad> quads = getModelQuads(model, state, face);
            processFaces(layers, face, quads);
        }

        final ModelQuadLayer[] quadLayers = new ModelQuadLayer[layers.size()];
        for (int layerIndex = 0; layerIndex < layers.size(); layerIndex++)
        {
            quadLayers[layerIndex] = layers.get(layerIndex).build(state, color, lv);
        }

        return quadLayers;
    }

    private static void processFaces(
      final List<ModelQuadLayer.ModelQuadLayerBuilder> layers,
      final Direction face,
      final List<BakedQuad> quads)
    {
        for ( final BakedQuad q : quads )
        {
            if (q.getFace() != face)
                return;

            try
            {
                final TextureAtlasSprite sprite = findQuadTexture( q );

                ModelQuadLayer.ModelQuadLayerBuilder b = null;
                for ( final ModelQuadLayer.ModelQuadLayerBuilder lx : layers )
                {
                    if ( lx.cache.getSprite() == sprite )
                    {
                        b = lx;
                        break;
                    }
                }

                if ( b == null )
                {
                    // top/bottom
                    int uCoord = 0;
                    int vCoord = 2;

                    switch ( face )
                    {
                        case NORTH:
                        case SOUTH:
                            uCoord = 0;
                            vCoord = 1;
                            break;
                        case EAST:
                        case WEST:
                            uCoord = 1;
                            vCoord = 2;
                            break;
                        default:
                    }

                    b = new ModelQuadLayer.ModelQuadLayerBuilder( sprite, uCoord, vCoord );
                    b.cache.setTint(q.getTintIndex());
                    layers.add( b );
                }

                q.pipe( b.uvr );

                if ( Configuration.getInstance().getClient().enableFaceLightmapExtraction.get() )
                {
                    b.lv.setVertexFormat(DefaultVertexFormats.BLOCK);
                    q.pipe( b.lv );
                }
            }
            catch ( final Exception ignored)
            {
            }
        }
    }

    private static IBakedModel solveModel(
      final BlockState state,
      final IBakedModel originalModel)
    {
        boolean hasFaces;
        try
        {
            hasFaces = hasFaces(originalModel, state, null);

            for (final Direction f : Direction.values())
            {
                hasFaces = hasFaces || hasFaces(originalModel, state, f);
            }
        }
        catch (final Exception e)
        {
            // an exception was thrown.. use the item model and hope...
            hasFaces = false;
        }

        if (!hasFaces)
        {
            // if the model is empty then lets grab an item and try that...
            final ItemStack is = ItemStackUtils.getItemStackFromBlockState(state);
            if (!is.isEmpty())
            {
                final IBakedModel itemModel =
                  Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(is, Minecraft.getInstance().world, Minecraft.getInstance().player);

                try
                {
                    hasFaces = hasFaces(originalModel, state, null);

                    for (final Direction f : Direction.values())
                    {
                        hasFaces = hasFaces || hasFaces(originalModel, state, f);
                    }
                }
                catch (final Exception e)
                {
                    // an exception was thrown.. use the item model and hope...
                    hasFaces = false;
                }

                if (hasFaces)
                {
                    return itemModel;
                }
                else
                {
                    return new SimpleGeneratedModel(findTexture(state, originalModel, Direction.UP));
                }
            }
        }

        return originalModel;
    }

    private static boolean hasFaces(
      final IBakedModel model,
      final BlockState state,
      final Direction f)
    {
        final List<BakedQuad> l = getModelQuads(model, state, f);
        if (l == null || l.isEmpty())
        {
            return false;
        }

        TextureAtlasSprite texture = null;

        try
        {
            texture = findTexture(null, l, f);
        }
        catch (final Exception ignored)
        {
        }

        final ModelVertexRange mvr = new ModelVertexRange();

        for (final BakedQuad q : l)
        {
            q.pipe(mvr);
        }

        return mvr.getLargestRange() > 0 && !isMissingTexture(texture);
    }

    public static TextureAtlasSprite findTexture(
      final BlockState state,
      final IBakedModel model,
      final Direction myFace)
    {
        TextureAtlasSprite texture = null;

        if (model != null)
        {
            try
            {
                texture = findTexture(null, getModelQuads(model, state, myFace), myFace);

                if (texture == null)
                {
                    for (final Direction side : Direction.values())
                    {
                        texture = findTexture(texture, getModelQuads(model, state, side), side);
                    }

                    texture = findTexture(texture, getModelQuads(model, state, null), null);
                }
            }
            catch (final Exception ignored)
            {
            }
        }

        // who knows if that worked.. now lets try to get a texture...
        if (isMissingTexture(texture))
        {
            try
            {
                if (model != null)
                {
                    texture = model.getParticleTexture();
                }
            }
            catch (final Exception ignored)
            {
            }
        }

        if (isMissingTexture(texture))
        {
            try
            {
                texture = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
            }
            catch (final Exception ignored)
            {
            }
        }

        if (texture == null)
        {
            texture = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(new ResourceLocation("missingno"));
        }

        return texture;
    }

    private static TextureAtlasSprite findTexture(
      TextureAtlasSprite texture,
      final List<BakedQuad> faceQuads,
      final Direction myFace) throws IllegalArgumentException, NullPointerException
    {
        for (final BakedQuad q : faceQuads)
        {
            if (q.getFace() == myFace)
            {
                texture = findQuadTexture(q);
            }
        }

        return texture;
    }

    private static TextureAtlasSprite findQuadTexture(
      final BakedQuad q) throws IllegalArgumentException, NullPointerException
    {
        if (q.sprite == null)
            return Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(MissingTextureSprite.getLocation());
        return q.sprite;
    }

    private static boolean isMissingTexture(final TextureAtlasSprite sprite) {
        if (sprite == null)
            return true;

        return sprite.getName().equals(MissingTextureSprite.getLocation());
    }

    private static List<BakedQuad> getModelQuads(
      final IBakedModel model,
      final BlockState state,
      final Direction f)
    {
        try
        {
            // try to get block model...
            return model.getQuads( state, f, RANDOM );
        }
        catch ( final Throwable ignored)
        {
        }

        try
        {
            // try to get item model?
            return model.getQuads( null, f, RANDOM );
        }
        catch ( final Throwable ignored)
        {
        }

        final ItemStack is = ItemStackUtils.getItemStackFromBlockState( state );
        if ( !is.isEmpty() )
        {
            final IBakedModel secondModel = getOverrides( model ).getOverrideModel( model, is, Minecraft.getInstance().world, Minecraft.getInstance().player );

            if ( secondModel != null )
            {
                try
                {
                    return secondModel.getQuads( null, f, RANDOM );
                }
                catch ( final Throwable ignored)
                {
                }
            }
        }

        // try to not crash...
        return Collections.emptyList();
    }

    private static ItemOverrideList getOverrides(
      final IBakedModel model )
    {
        if ( model != null )
        {
            return model.getOverrides();
        }
        return ItemOverrideList.EMPTY;
    }

    private int getColorFor(
      final BlockState state)
    {
        try
        {
            return colorCache.get(state, () -> {
                int out;
                final Fluid fluid = state.getFluidState().getFluid();
                if ( fluid != Fluids.EMPTY )
                {
                    out = fluid.getAttributes().getColor();
                }
                else
                {
                    final ItemStack target = ItemStackUtils.getItemStackFromBlockState( state );

                    if ( target.isEmpty() )
                    {
                        out = 0xffffff;
                    }
                    else
                    {
                        out = Minecraft.getInstance().getItemColors().getColor( target, 0);
                    }
                }

                return out;
            });
        }
        catch (ExecutionException e)
        {
            LOGGER.warn("Failed to determine the color of a blockstate.", e);
            return 0;
        }
    }
}
