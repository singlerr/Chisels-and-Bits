package mod.chiselsandbits.integration.chiselsandbits.create;

import com.jozufozu.flywheel.backend.material.MaterialSpec;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.event.GatherContextEvent;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateMaterials
{
    public static final MaterialSpec<ModelData> TRANSFORMED = new MaterialSpec<>(Locations.MODEL, CreatePrograms.TRANSFORMED, Formats.COLORED_LIT_MODEL, Formats.TRANSFORMED, ModelData::new);

    public static void flwInit(GatherContextEvent event) {
        event.getBackend()
          .register(TRANSFORMED);
    }

    public static class Locations {
        public static final ResourceLocation MODEL    = new ResourceLocation(Constants.MOD_ID, "model");
    }
}
