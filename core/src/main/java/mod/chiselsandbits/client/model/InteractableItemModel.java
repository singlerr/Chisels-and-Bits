package mod.chiselsandbits.client.model;

import com.communi.suggestu.scena.core.client.models.loaders.IModelSpecification;
import com.communi.suggestu.scena.core.client.models.loaders.context.IModelBakingContext;
import com.mojang.datafixers.util.Pair;
import mod.chiselsandbits.client.model.baked.interactable.InteractableBakedItemModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import static net.minecraft.client.resources.model.ModelBakery.GENERATION_MARKER;

public class InteractableItemModel implements IModelSpecification<InteractableItemModel>
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

    private ResourceLocation innerModelLocation;
    private UnbakedModel innerModel;

    public InteractableItemModel(final ResourceLocation innerModelLocation)
    {
        this.innerModelLocation = innerModelLocation;
    }

    @Override
    public BakedModel bake(IModelBakingContext iModelBakingContext, ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ResourceLocation modelLocation) {
        if (this.innerModel instanceof BlockModel blockModel) {
            if (blockModel.getRootModel() == GENERATION_MARKER) {
                return new InteractableBakedItemModel(ITEM_MODEL_GENERATOR.generateBlockModel(spriteGetter, blockModel).bake(modelBakery, blockModel, spriteGetter, modelState, modelLocation, false));
            }
        }

        final BakedModel innerBakedModel = this.innerModel.bake(
                modelBakery,
                spriteGetter,
                modelState,
                innerModelLocation
        );

        return new InteractableBakedItemModel(innerBakedModel);
    }

    @Override
    public Collection<Material> getTextures(IModelBakingContext iModelBakingContext, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        this.innerModel = modelGetter.apply(this.innerModelLocation);
        if (this.innerModel == null) {
            LOGGER.warn("No parent '{}' while loading model '{}'", this.innerModelLocation, this);
        }

        if (this.innerModel == null) {
            this.innerModelLocation = ModelBakery.MISSING_MODEL_LOCATION;
            this.innerModel = modelGetter.apply(this.innerModelLocation);
        }

        if (!(this.innerModel instanceof BlockModel)) {
            throw new IllegalStateException("BlockModel parent has to be a block model.");
        }

        return innerModel.getMaterials(modelGetter, missingTextureErrors);
    }
}
