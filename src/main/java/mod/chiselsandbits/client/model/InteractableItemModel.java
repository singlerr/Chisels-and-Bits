package mod.chiselsandbits.client.model;

import com.mojang.datafixers.util.Pair;
import mod.chiselsandbits.client.model.baked.interactable.InteractableBakedItemModel;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class InteractableItemModel implements IModelGeometry<InteractableItemModel>
{
    private static final Logger LOGGER = LogManager.getLogger();

    private ResourceLocation innerModelLocation;
    private IUnbakedModel innerModel;

    public InteractableItemModel(final ResourceLocation innerModelLocation)
    {
        this.innerModelLocation = innerModelLocation;
    }

    @Override
    public IBakedModel bake(
      final IModelConfiguration owner,
      final ModelBakery bakery,
      final Function<RenderMaterial, TextureAtlasSprite> spriteGetter,
      final IModelTransform modelTransform,
      final ItemOverrideList overrides,
      final ResourceLocation modelLocation)
    {
        final IBakedModel innerBakedModel = this.innerModel.bakeModel(
          bakery,
          spriteGetter,
          modelTransform,
          innerModelLocation
        );

        return new InteractableBakedItemModel(innerBakedModel);
    }

    @Override
    public Collection<RenderMaterial> getTextures(
      final IModelConfiguration owner, final Function<ResourceLocation, IUnbakedModel> modelGetter, final Set<Pair<String, String>> missingTextureErrors)
    {
        this.innerModel = modelGetter.apply(this.innerModelLocation);
        if (this.innerModel == null) {
            LOGGER.warn("No parent '{}' while loading model '{}'", this.innerModelLocation, this);
        }

        if (this.innerModel == null) {
            this.innerModelLocation = ModelBakery.MODEL_MISSING;
            this.innerModel = modelGetter.apply(this.innerModelLocation);
        }

        if (!(this.innerModel instanceof BlockModel)) {
            throw new IllegalStateException("BlockModel parent has to be a block model.");
        }

        return innerModel.getTextures(modelGetter, missingTextureErrors);
    }
}
