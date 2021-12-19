package mod.chiselsandbits.forge.integration.chiselsandbits.create;

import com.jozufozu.flywheel.api.struct.*;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.core.materials.model.writer.UnsafeModelWriter;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.event.GatherContextEvent;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class CreateMaterials
{
    public static final StructType<ModelData> TRANSFORMED = new TransformedType();

    public static void flwInit(GatherContextEvent event)
    {
        event.getBackend()
          .register(Locations.MODEL, TRANSFORMED);
    }

    public static class Locations
    {
        public static final ResourceLocation MODEL = new ResourceLocation(Constants.MOD_ID, "model");
    }

    private static final class TransformedType implements Instanced<ModelData>, Batched<ModelData>
    {
        @Override
        public @NotNull ModelData create()
        {
            return new ModelData();
        }

        @Override
        public @NotNull VertexFormat format()
        {
            return Formats.TRANSFORMED;
        }

        @Override
        public @NotNull StructWriter<ModelData> getWriter(@NotNull VecBuffer backing)
        {
            return new UnsafeModelWriter(backing, this);
        }

        @Override
        public @NotNull ResourceLocation getProgramSpec()
        {
            return CreatePrograms.TRANSFORMED;
        }

        @SuppressWarnings("NullableProblems")
        @Nullable
        @Override
        public BatchingTransformer<ModelData> getTransformer(@NotNull Model model)
        {
            return null;
        }
    }
}
