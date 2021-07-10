package mod.chiselsandbits.data.icons;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.chiselsandbits.api.util.ReflectionUtils;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.AtlasTexture.SheetData;
import net.minecraft.client.renderer.texture.SpriteMap;
import net.minecraft.client.renderer.tileentity.BellTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.ConduitTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.EnchantmentTableTileEntityRenderer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ModelLoader
{
    private final IResourceManager resourceManager;
    private final DummyModelBakery bakery;
    private final List<ResourceLocation> modelLocations = new ArrayList<>();
    private final Map<ResourceLocation, IBakedModel> bakedModels = new Object2ObjectOpenHashMap<>();
    protected static final Set<RenderMaterial> LOCATIONS_BUILTIN_TEXTURES = Util.make(Sets.newHashSet(), (materialSet) -> {
        materialSet.add(ModelBakery.LOCATION_WATER_FLOW);
        materialSet.add(ModelBakery.LOCATION_LAVA_FLOW);
        materialSet.add(ModelBakery.LOCATION_WATER_OVERLAY);
        materialSet.add(ModelBakery.LOCATION_FIRE_0);
        materialSet.add(ModelBakery.LOCATION_FIRE_1);
        materialSet.add(BellTileEntityRenderer.BELL_BODY_TEXTURE);
        materialSet.add(ConduitTileEntityRenderer.BASE_TEXTURE);
        materialSet.add(ConduitTileEntityRenderer.CAGE_TEXTURE);
        materialSet.add(ConduitTileEntityRenderer.WIND_TEXTURE);
        materialSet.add(ConduitTileEntityRenderer.VERTICAL_WIND_TEXTURE);
        materialSet.add(ConduitTileEntityRenderer.OPEN_EYE_TEXTURE);
        materialSet.add(ConduitTileEntityRenderer.CLOSED_EYE_TEXTURE);
        materialSet.add(EnchantmentTableTileEntityRenderer.TEXTURE_BOOK);
        materialSet.add(ModelBakery.LOCATION_BANNER_BASE);
        materialSet.add(ModelBakery.LOCATION_SHIELD_BASE);
        materialSet.add(ModelBakery.LOCATION_SHIELD_NO_PATTERN);

        for(ResourceLocation resourcelocation : ModelBakery.DESTROY_STAGES) {
            materialSet.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, resourcelocation));
        }

        materialSet.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET));
        materialSet.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE));
        materialSet.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS));
        materialSet.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS));
        materialSet.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD));
        Atlases.collectAllMaterials(materialSet::add);
    });
    private Map<ResourceLocation, Pair<AtlasTexture, AtlasTexture.SheetData>> internalSheets;

    public ModelLoader(IResourceManager manager)
    {
        this.resourceManager = manager;
        this.bakery = new DummyModelBakery(this.resourceManager, Minecraft.getInstance().getBlockColors());
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
        Set<RenderMaterial> renderMaterials = unbaked.values().stream().flatMap((model) -> model.getTextures(bakery::getUnbakedModel, missing).stream()).collect(Collectors.toSet());
        renderMaterials.addAll(LOCATIONS_BUILTIN_TEXTURES);
        net.minecraftforge.client.ForgeHooksClient.gatherFluidTextures(renderMaterials);
        Map<ResourceLocation, List<RenderMaterial>> materialsByAtlas = renderMaterials.stream().collect(Collectors.groupingBy(RenderMaterial::getAtlasLocation));

        internalSheets = Maps.newHashMap();

        for(Entry<ResourceLocation, List<RenderMaterial>> entry : materialsByAtlas.entrySet()) {
            AtlasTexture atlastexture = new AtlasTexture(entry.getKey());
            AtlasTexture.SheetData atlasSheetData = atlastexture.stitch(this.resourceManager, entry.getValue().stream().map(RenderMaterial::getTextureLocation), EmptyProfiler.INSTANCE, 0);
            internalSheets.put(entry.getKey(), Pair.of(atlastexture, atlasSheetData));
        }

        for(Pair<AtlasTexture, AtlasTexture.SheetData> pair : internalSheets.values()) {
            AtlasTexture atlastexture = pair.getFirst();
            AtlasTexture.SheetData atlastexture$sheetdata = pair.getSecond();
            atlastexture.upload(atlastexture$sheetdata);
            Minecraft.getInstance().getTextureManager().loadTexture(atlastexture.getTextureLocation(), atlastexture);
            Minecraft.getInstance().getTextureManager().bindTexture(atlastexture.getTextureLocation());
            atlastexture.setBlurMipmap(atlastexture$sheetdata);
        }

        SpriteMap sheetData = new SpriteMap(internalSheets.values().stream().map(Pair::getFirst).collect(Collectors.toList()));
        bakery.setSpriteMap(sheetData);
        ReflectionUtils.setField(Minecraft.getInstance().getModelManager(), "atlases", bakery.getSpriteMap());
        for(Entry<ResourceLocation, IUnbakedModel> entry : unbaked.entrySet())
        {
            IBakedModel baked = entry.getValue().bakeModel(
              bakery, sheetData::getSprite, ModelRotation.X0_Y0, entry.getKey()
            );
            bakedModels.put(entry.getKey(), baked);
        }

        ReflectionUtils.setField(Minecraft.getInstance().getModelManager().getBlockModelShapes(), "bakedModelStore", this.bakedModels);
    }

    public Map<ResourceLocation, Pair<AtlasTexture, SheetData>> getInternalSheets()
    {
        return internalSheets;
    }

    public IBakedModel getModel(ResourceLocation loc)
    {
        return bakedModels.get(loc);
    }
}