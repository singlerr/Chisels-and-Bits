package mod.chiselsandbits.data.icons;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.AtlasTexture.SheetData;
import net.minecraft.client.renderer.texture.SpriteMap;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.Map.Entry;

public class ModelLoader
{
    private final IResourceManager resourceManager;
    private final DummyModelBakery bakery;
    private final List<ResourceLocation> modelLocations = new ArrayList<>();
    private final Map<ResourceLocation, IBakedModel> bakedModels = new Object2ObjectOpenHashMap<>();
    private final AtlasTexture itemAtlas  = new AtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);

    public ModelLoader(IResourceManager manager)
    {
        this.resourceManager = manager;
        this.bakery = new DummyModelBakery(this.resourceManager, BlockColors.init());
    }

    public void add(ResourceLocation location)
    {
        modelLocations.add(location);
    }

    public void bake()
    {
        Map<ResourceLocation, IUnbakedModel> unbaked = new Object2ObjectOpenHashMap<>();
        for(ResourceLocation modelLocation : modelLocations)
        {
            IUnbakedModel unbakedModel = bakery.getUnbakedModel(modelLocation);
            if (unbakedModel != null)
                unbaked.put(modelLocation, unbakedModel);
        }
        HashSet<Pair<String, String>> missing = new HashSet<>();
        final SheetData sheetData = itemAtlas.stitch(
          resourceManager,
          unbaked.values().stream()
            .flatMap(u -> {
                final Collection<RenderMaterial> textureData = u.getTextures(bakery::getUnbakedModel, missing);
                return textureData.stream();
            })
            .map(RenderMaterial::getTextureLocation),
          EmptyProfiler.INSTANCE,
          0
        );

        bakery.setSpriteMap(new SpriteMap(Lists.newArrayList(getAtlas())));
        itemAtlas.upload(sheetData);
        for(Entry<ResourceLocation, IUnbakedModel> entry : unbaked.entrySet())
        {
            IBakedModel baked = entry.getValue().bakeModel(
              bakery, mat -> itemAtlas.getSprite(mat.getTextureLocation()), ModelRotation.X0_Y0, entry.getKey()
            );
            bakedModels.put(entry.getKey(), baked);
        }
    }

    public AtlasTexture getAtlas()
    {
        return itemAtlas;
    }

    public IBakedModel getModel(ResourceLocation loc)
    {
        return bakedModels.get(loc);
    }
}