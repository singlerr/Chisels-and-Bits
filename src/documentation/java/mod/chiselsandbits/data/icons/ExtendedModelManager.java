package mod.chiselsandbits.data.icons;

import mod.chiselsandbits.api.util.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.SpriteMap;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Map;

public class ExtendedModelManager extends ModelManager
{
    private SpriteMap spriteMap = null;

    public ExtendedModelManager(
      final TextureManager textureManagerIn,
      final BlockColors blockColorsIn,
      final int maxMipmapLevelIn)
    {
        super(textureManagerIn, blockColorsIn, maxMipmapLevelIn);
    }

    public void loadModels() {
        final ModelBakery modelBakery = this.prepare(
          Minecraft.getInstance().getResourceManager(),
          EmptyProfiler.INSTANCE
        );

        this.apply(
          modelBakery,
          Minecraft.getInstance().getResourceManager(),
          EmptyProfiler.INSTANCE
        );

        this.spriteMap = modelBakery.getSpriteMap();

        Minecraft.getInstance().getItemRenderer().getItemModelMesher().rebuildCache();
    }

    @SuppressWarnings("unchecked")
    public Collection<ResourceLocation> getTextureMap()
    {
        if (spriteMap == null)
            throw new IllegalStateException("SpriteMap not initialized.");

        final Map<ResourceLocation, AtlasTexture> textureMap = (Map<ResourceLocation, AtlasTexture>) ReflectionUtils.getField(spriteMap, "atlasTextures");
        return textureMap.keySet();
    }
}
