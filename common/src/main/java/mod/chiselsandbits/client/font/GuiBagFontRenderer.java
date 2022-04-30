package mod.chiselsandbits.client.font;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.math.Matrix4f;
import org.jetbrains.annotations.NotNull;

public class GuiBagFontRenderer extends Font
{
    private final Font fontRenderer;

    private final int offsetX, offsetY;
    private final float scale;

    public GuiBagFontRenderer(
      final Font src,
      final int bagStackSize)
    {
        super(src.fonts);
        fontRenderer = src;

        if (bagStackSize < 100)
        {
            scale = 1f;
            offsetX = 0;
            offsetY = 0;
        }
        else
        {
            scale = 0.75f;
            offsetX = 3;
            offsetY = 2;
        }
    }

    @Override
    public int drawShadow(PoseStack matrixStack, @NotNull String text, float x, float y, int color)
    {
        try
        {
            text = convertText(text);
            matrixStack.pushPose();
            matrixStack.scale(scale, scale, scale);

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return fontRenderer.drawShadow(matrixStack, text, x, y, color);
        }
        finally
        {
            matrixStack.popPose();
        }
    }

    @Override
    public int draw(PoseStack matrixStack, @NotNull String text, float x, float y, int color)
    {
        try
        {
            text = convertText(text);
            matrixStack.pushPose();
            matrixStack.scale(scale, scale, scale);

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return fontRenderer.draw(matrixStack, text, x, y, color);
        }
        finally
        {
            matrixStack.popPose();
        }
    }

    @Override
    public int drawInternal(final @NotNull String text, float x, float y, final int color, final @NotNull Matrix4f matrix, final boolean dropShadow, final boolean p_228078_7_)
    {
        final PoseStack stack = new PoseStack();
        final Matrix4f original = new Matrix4f(matrix);

        try
        {
            stack.last().pose().multiply(matrix);
            stack.scale(scale, scale, scale);

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return super.drawInternal(text, x, y, color, stack.last().pose(), dropShadow, p_228078_7_);
        }
        finally
        {
            matrix.setIdentity();
            matrix.multiply(original);
        }
    }

    @Override
    public int drawInBatch(
      final @NotNull String text,
      float x,
      float y,
      final int color,
      final boolean dropShadow,
      final @NotNull Matrix4f matrix,
      final @NotNull MultiBufferSource buffer,
      final boolean transparentIn,
      final int colorBackgroundIn,
      final int packedLight)
    {
        final PoseStack stack = new PoseStack();
        final Matrix4f original = new Matrix4f(matrix);

        try
        {
            stack.last().pose().multiply(matrix);
            stack.scale(scale, scale, scale);

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return super.drawInBatch(convertText(text), x, y, color, dropShadow, stack.last().pose(), buffer, transparentIn, colorBackgroundIn, packedLight);
        }
        finally
        {
            matrix.setIdentity();
            matrix.multiply(original);
        }
    }

    @Override
    public int width(
      @NotNull String text)
    {
        text = convertText(text);
        return fontRenderer.width(text);
    }

    private String convertText(
      final String text)
    {
        try
        {
            final int value = Integer.parseInt(text);

            if (value >= 1000)
            {
                return value / 1000 + "k";
            }

            return text;
        }
        catch (final NumberFormatException e)
        {
            return text;
        }
    }
}
