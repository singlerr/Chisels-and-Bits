package mod.chiselsandbits.registrars;

import mod.chiselsandbits.platforms.core.client.models.data.IModelDataKey;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;

import java.util.Map;

public final class ModModelProperties
{

    private ModModelProperties()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModModelProperties. This is a utility class");
    }

    public static IModelDataKey<BakedModel>                  UNKNOWN_LAYER_MODEL_PROPERTY = IModelDataKey.create();
    public static IModelDataKey<Map<RenderType, BakedModel>> KNOWN_LAYER_MODEL_PROPERTY   = IModelDataKey.create();
}
