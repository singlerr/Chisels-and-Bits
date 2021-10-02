package mod.chiselsandbits.registrars;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Map;

public final class ModModelProperties
{

    private ModModelProperties()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModModelProperties. This is a utility class");
    }

    public static ModelProperty<IBakedModel>                  UNKNOWN_LAYER_MODEL_PROPERTY = new ModelProperty<>();
    public static ModelProperty<Map<RenderType, IBakedModel>> KNOWN_LAYER_MODEL_PROPERTY   = new ModelProperty<>();
}
