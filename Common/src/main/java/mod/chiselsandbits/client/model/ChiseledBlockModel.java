package mod.chiselsandbits.client.model;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import mod.chiselsandbits.client.model.baked.chiseled.DataAwareChiseledBlockBakedModel;
import mod.chiselsandbits.platforms.core.client.models.loaders.IModelSpecification;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class ChiseledBlockModel implements IModelSpecification<ChiseledBlockModel>
{
    @Override
    public BakedModel bake(
      final ModelBakery bakery,
      final Function<Material, TextureAtlasSprite> spriteGetter,
      final ModelState modelTransform,
      final ItemOverrides overrides,
      final ResourceLocation modelLocation)
    {
        return new DataAwareChiseledBlockBakedModel();
    }

    @Override
    public Collection<Material> getTextures(
      final Function<ResourceLocation, UnbakedModel> modelGetter, final Set<Pair<String, String>> missingTextureErrors)
    {
        //We are not injecting our own textures.
        //So this is irrelevant.
        return ImmutableSet.of();
    }
}
