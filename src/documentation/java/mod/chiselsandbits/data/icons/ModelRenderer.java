package mod.chiselsandbits.data.icons;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.model.TransformationHelper;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

import static org.lwjgl.opengl.GL40.*;

public class ModelRenderer
{
    private static final Direction[] DIRECTIONS_AND_NULL = Arrays.copyOf(Direction.values(), 7);
    private static final Random RANDOM = new Random(0);

    private final int width;
    private final int height;
    private final AtlasTexture texture;
    private final int framebufferID;
    private final int renderedTexture;
    private final int depthBuffer;
    private final File outputDirectory;

    public ModelRenderer(int width, int height, final File outputDirectory, AtlasTexture texture)
    {
        this.width = width;
        this.height = height;
        this.outputDirectory = outputDirectory;
        this.texture = texture;
        this.framebufferID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);

        this.renderedTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, renderedTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, renderedTexture, 0);

        this.depthBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        //TODO do we need/want stencil?
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);
    }

    // TODO free GL resources

    public void renderModel(IBakedModel model, String filename)
    {
        if (model == null)
            return;

        // Set up GL
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        glViewport(0, 0, width, height);
        glBindTexture(GL_TEXTURE_2D, texture.getGlTextureId());
        glClearColor(0.0f,0.0f,0.0f,0.0f);
        glClearDepth(1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        // TODO figure out where the .25 comes from. Maybe blocks always render too big?
        glOrtho(-1.25, 1.25, -1.25, 1.25, -1000, 1000);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        RenderHelper.enableStandardItemLighting();
        RenderHelper.setupGui3DDiffuseLighting();
        glDepthFunc(GL_LEQUAL);

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);

        // Actually render
        MatrixStack stack = new MatrixStack();
        boolean sideLitModel = !model.isSideLit();
        if (sideLitModel) {
            RenderHelper.setupGuiFlatDiffuseLighting();
        }
        else
        {
            RenderHelper.setupGui3DDiffuseLighting();
        }

        final TransformationMatrix tr = TransformationHelper.toTransformation(model.getBakedModel().getItemCameraTransforms().getTransform(TransformType.GUI));
        final Vector3f translationApplied = tr.getTranslation();

        if (!translationApplied.equals(new Vector3f())) {
            translationApplied.mul(-1);
            RenderSystem.translatef(
              translationApplied.getX(),
              translationApplied.getY(),
              translationApplied.getZ()
            );
        }

        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.rotatef(180, 0, 0, 1);

        if (tr.isIdentity()) {
            RenderSystem.rotatef(180, 0, 1,0);
        }

        //Deal with none normal Transformtypes
        model = model.handlePerspective(TransformType.GUI, stack);

        bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.BLOCK);
        for(Direction side : DIRECTIONS_AND_NULL)
            for(BakedQuad quad : model.getQuads(null, side, RANDOM, EmptyModelData.INSTANCE))
                bufferbuilder.addQuad(
                  stack.getLast(), quad, 1, 1, 1, 15728880, OverlayTexture.NO_OVERLAY
                );
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        if (sideLitModel) {
            RenderHelper.setupGui3DDiffuseLighting();
        }

        final TextureCutter cutter = new TextureCutter(256, 256);
        exportTo(filename, renderedTexture, cutter::cutTexture);
    }

    public void exportAtlas() {
        exportTo("atlas.png", texture.getGlTextureId(), bufferedImage -> bufferedImage);
    }

    private void exportTo(String fileName, int texture, Function<BufferedImage, BufferedImage> imageAdapter)
    {
        glBindTexture(GL_TEXTURE_2D, texture);
        int width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
        int size = width*height;
        BufferedImage bufferedimage = new BufferedImage(width, height, 2);

        File output = new File(this.outputDirectory, fileName);
        IntBuffer buffer = BufferUtils.createIntBuffer(size);
        int[] data = new int[size];

        glGetTexImage(GL_TEXTURE_2D, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
        buffer.get(data);
        bufferedimage.setRGB(0, 0, width, height, data, 0, width);

        try
        {
            output.getParentFile().mkdirs();
            if (output.exists())
                output.delete();
            ImageIO.write(imageAdapter.apply(bufferedimage), "png", output);
        } catch(IOException xcp)
        {
            throw new RuntimeException(xcp);
        }
    }
}