package mod.chiselsandbits.client.render;

import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;
import java.util.function.Supplier;

public enum ModRenderTypes
{
    MEASUREMENT_LINES(() -> Internal.MEASUREMENT_LINES);

    private final Supplier<RenderType> typeSupplier;

    ModRenderTypes(final Supplier<RenderType> typeSupplier) {this.typeSupplier = typeSupplier;}

    public RenderType get() {
        return typeSupplier.get();
    }

    private static class Internal extends RenderType
    {
        private static final RenderType MEASUREMENT_LINES = RenderType.makeType(Constants.MOD_ID + ":measurement_lines",
          DefaultVertexFormats.POSITION_COLOR,
          1,
          256,
          RenderType.State.getBuilder()
            .line(new RenderState.LineState(OptionalDouble.empty()))
            .layer(field_239235_M_)
            .transparency(GLINT_TRANSPARENCY)
            .target(field_239236_S_)
            .writeMask(COLOR_WRITE)
            .cull(CULL_DISABLED)
            .depthTest(RenderState.DEPTH_ALWAYS)
            .fog(NO_FOG)
            .build(false));

        private Internal(String name, VertexFormat fmt, int glMode, int size, boolean doCrumbling, boolean depthSorting, Runnable onEnable, Runnable onDisable)
        {
            super(name, fmt, glMode, size, doCrumbling, depthSorting, onEnable, onDisable);
            throw new IllegalStateException("This class must not be instantiated");
        }
    }
}
