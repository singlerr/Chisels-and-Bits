package mod.chiselsandbits.client.model;

import com.communi.suggestu.scena.core.client.models.loaders.IModelSpecification;
import com.communi.suggestu.scena.core.client.models.loaders.context.IModelBakingContext;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import mod.chiselsandbits.client.model.baked.chiseled.DataAwareChiseledBlockBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class ChiseledBlockModel implements IModelSpecification<ChiseledBlockModel>
{
    @Override
    public BakedModel bake(IModelBakingContext iModelBakingContext, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation) {
        return new DataAwareChiseledBlockBakedModel();
    }
}
