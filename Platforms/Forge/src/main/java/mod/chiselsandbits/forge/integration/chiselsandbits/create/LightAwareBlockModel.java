package mod.chiselsandbits.forge.integration.chiselsandbits.create;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.jozufozu.flywheel.util.RenderMath;
import com.jozufozu.flywheel.util.VirtualEmptyModelData;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS;
import static com.mojang.blaze3d.vertex.VertexFormat.Mode.TRIANGLE_STRIP;

public class LightAwareBlockModel implements Model
{
    private static final PoseStack IDENTITY = new PoseStack();
    private static final AtomicInteger id = new AtomicInteger(0);

    private final String name;
    private final BufferBuilderReader reader;

    public LightAwareBlockModel(BakedModel model, BlockState referenceState)
    {
        this(model, referenceState, IDENTITY);
    }

    public LightAwareBlockModel(BakedModel model, BlockState referenceState, PoseStack ms)
    {
        reader = new BufferBuilderReader(getBufferBuilder(model, referenceState, ms));
        this.name = "LightAwareBlockModel" + id.incrementAndGet();
    }

    @Override
    public @NotNull VertexFormat format()
    {
        return Formats.COLORED_LIT_MODEL;
    }

    @Override
    public int vertexCount()
    {
        return reader.getVertexCount();
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public void buffer(@NotNull VertexConsumer vertices)
    {
        for (int i = 0; i < vertexCount(); i++) {
            vertices.vertex(reader.getX(i), reader.getY(i), reader.getZ(i));

            vertices.normal(RenderMath.f(reader.getNX(i)), RenderMath.f(reader.getNY(i)), RenderMath.f(reader.getNZ(i)));

            vertices.uv(reader.getU(i), reader.getV(i));

            vertices.color(reader.getR(i), reader.getG(i), reader.getB(i), reader.getA(i));

            int light = reader.getLight(i);

            byte block = (byte) (LightTexture.block(light) << 4);
            byte sky = (byte) (LightTexture.sky(light) << 4);

            vertices.uv2(block, sky);

            vertices.endVertex();
        }
    }

    public BufferBuilder getBufferBuilder(BakedModel model, BlockState referenceState, PoseStack ms)
    {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        ModelBlockRenderer blockRenderer = dispatcher.getModelRenderer();
        BufferBuilder builder = new BufferBuilder(512);

        builder.begin(QUADS, DefaultVertexFormat.BLOCK);
        blockRenderer.renderModel(
          ms.last(),
          builder,
          referenceState,
          model,
          1f,
          1f,
          1f,
          LightTexture.FULL_BRIGHT,
          OverlayTexture.NO_OVERLAY,
          VirtualEmptyModelData.INSTANCE);
        builder.end();
        return builder;
    }
}
