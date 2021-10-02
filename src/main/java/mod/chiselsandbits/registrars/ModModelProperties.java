package mod.chiselsandbits.registrars;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Map;

public final class ModModelProperties
{

    private ModModelProperties()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModModelProperties. This is a utility class");
    }

    public static ModelProperty<BakedModel>                   UNKNOWN_LAYER_MODEL_PROPERTY = new ModelProperty<>();
    public static ModelProperty<Map<RenderType, BakedModel>> KNOWN_LAYER_MODEL_PROPERTY   = new ModelProperty<>();
}
