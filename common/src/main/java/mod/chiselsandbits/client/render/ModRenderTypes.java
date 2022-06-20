package mod.chiselsandbits.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;
import java.util.function.Supplier;

public enum ModRenderTypes
{
    MEASUREMENT_LINES(() -> Internal.MEASUREMENT_LINES),
    WIREFRAME_LINES(() -> Internal.WIREFRAME_LINES),
    WIREFRAME_BODY(() -> Internal.WIREFRAME_BODY),
    GHOST_PREVIEW_BEHIND(() -> Internal.GHOST_PREVIEW_BEHIND);

    private final Supplier<RenderType> typeSupplier;

    ModRenderTypes(final Supplier<RenderType> typeSupplier) {this.typeSupplier = typeSupplier;}

    public RenderType get() {
        return typeSupplier.get();
    }

    private static class Internal extends RenderType
    {
        private static final RenderType MEASUREMENT_LINES = RenderType.create(Constants.MOD_ID + ":measurement_lines",
          DefaultVertexFormat.POSITION_COLOR_NORMAL,
          VertexFormat.Mode.LINES,
          256,
          false,
          false,
          RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_LINES_SHADER)
            .setLineState(new LineStateShard(OptionalDouble.of(2.5d)))
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setOutputState(TRANSLUCENT_TARGET)
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
            .createCompositeState(false));

        private static final RenderType WIREFRAME_LINES = RenderType.create(Constants.MOD_ID + ":wireframe_lines",
          DefaultVertexFormat.POSITION_COLOR,
          VertexFormat.Mode.LINES,
          256,
          false,
          true,
          CompositeState.builder()
            .setShaderState(RENDERTYPE_LINES_SHADER)
            .setLineState(new LineStateShard(OptionalDouble.of(3d)))
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setOutputState(TRANSLUCENT_TARGET)
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
            .createCompositeState(false));

        private static final RenderType WIREFRAME_BODY = RenderType.create(Constants.MOD_ID + ":wireframe_body",
          DefaultVertexFormat.BLOCK,
          VertexFormat.Mode.QUADS,
          2097152,
          false,
          true,
          CompositeState.builder()
            .setShaderState(RenderType.RENDERTYPE_SOLID_SHADER)
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(NO_TRANSPARENCY)
            .setOutputState(TRANSLUCENT_TARGET)
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(NO_DEPTH_TEST)
            .createCompositeState(false));

        // Only difference from RenderType#ENTITY_TRANSLUCENT_CULL is GL11#GL_GREATER for its depth function, rather than GL11#GL_LEQUAL
        private static final RenderType GHOST_PREVIEW_BEHIND = RenderType.create(Constants.MOD_ID + ":ghost_preview_behind",
          DefaultVertexFormat.NEW_ENTITY,
          VertexFormat.Mode.QUADS,
          256,
          true,
          true,
          CompositeState.builder()
            .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .setDepthTestState(new RenderStateShard.DepthTestStateShard(">", GL11.GL_GREATER))
            .createCompositeState(true));

        private Internal(String name, VertexFormat fmt, VertexFormat.Mode glMode, int size, boolean doCrumbling, boolean depthSorting, Runnable onEnable, Runnable onDisable)
        {
            super(name, fmt, glMode, size, doCrumbling, depthSorting, onEnable, onDisable);
            throw new IllegalStateException("This class must not be instantiated");
        }
    }
}
