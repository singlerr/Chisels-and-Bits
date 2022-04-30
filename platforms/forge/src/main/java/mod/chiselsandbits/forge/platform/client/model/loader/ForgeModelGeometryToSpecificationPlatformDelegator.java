package mod.chiselsandbits.forge.platform.client.model.loader;

import com.mojang.datafixers.util.Pair;
import mod.chiselsandbits.platforms.core.client.models.loaders.IModelSpecification;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public final class ForgeModelGeometryToSpecificationPlatformDelegator<T extends IModelSpecification<T>>
  implements IModelGeometry<ForgeModelGeometryToSpecificationPlatformDelegator<T>>
{

    private final T delegate;

    public ForgeModelGeometryToSpecificationPlatformDelegator(final T delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public BakedModel bake(
      final IModelConfiguration owner,
      final ModelBakery bakery,
      final Function<Material, TextureAtlasSprite> spriteGetter,
      final ModelState modelTransform,
      final ItemOverrides overrides,
      final ResourceLocation modelLocation)
    {
        return new ForgeBakedModelDelegate(delegate.bake(
          bakery, spriteGetter, modelTransform, modelLocation
        ));
    }

    @Override
    public Collection<Material> getTextures(
      final IModelConfiguration owner, final Function<ResourceLocation, UnbakedModel> modelGetter, final Set<Pair<String, String>> missingTextureErrors)
    {
        return delegate.getTextures(
          modelGetter, missingTextureErrors
        );
    }
}
