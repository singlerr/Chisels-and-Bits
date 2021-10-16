package mod.chiselsandbits.integration.chiselsandbits.create;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.model.IModel;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.jozufozu.flywheel.util.VirtualEmptyModelData;
import com.mojang.blaze3d.matrix.MatrixStack;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

public class LightAwareBlockModel implements IModel
{
    private static final MatrixStack IDENTITY = new MatrixStack();

    private final BufferBuilderReader reader;

    public LightAwareBlockModel(IBakedModel model, BlockState referenceState)
    {
        this(model, referenceState, IDENTITY);
    }

    public LightAwareBlockModel(IBakedModel model, BlockState referenceState, MatrixStack ms)
    {
        reader = new BufferBuilderReader(getBufferBuilder(model, referenceState, ms));
    }

    @Override
    public VertexFormat format()
    {
        return Formats.COLORED_LIT_MODEL;
    }

    @Override
    public int vertexCount()
    {
        return reader.getVertexCount();
    }

    @Override
    public void buffer(VecBuffer buffer)
    {
        for (int i = 0; i < vertexCount(); i++) {
            buffer.putVec3(reader.getX(i), reader.getY(i), reader.getZ(i));

            buffer.putVec3(reader.getNX(i), reader.getNY(i), reader.getNZ(i));

            buffer.putVec2(reader.getU(i), reader.getV(i));

            buffer.putColor(reader.getR(i), reader.getG(i), reader.getB(i), reader.getA(i));

            int light = reader.getLight(i);

            byte block = (byte) (LightTexture.block(light) << 4);
            byte sky = (byte) (LightTexture.sky(light) << 4);

            buffer.putVec2(block, sky);
        }
    }

    public BufferBuilder getBufferBuilder(IBakedModel model, BlockState referenceState, MatrixStack ms)
    {
        Minecraft mc = Minecraft.getInstance();
        BlockRendererDispatcher dispatcher = mc.getBlockRenderer();
        BlockModelRenderer blockRenderer = dispatcher.getModelRenderer();
        BufferBuilder builder = new BufferBuilder(512);

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        blockRenderer.renderModel(
          mc.level,
          model,
          referenceState,
          BlockPos.ZERO.above(255),
          ms,
          builder,
          true,
          mc.level.random,
          42,
          OverlayTexture.NO_OVERLAY,
          VirtualEmptyModelData.INSTANCE);
        builder.end();
        return builder;
    }
}
