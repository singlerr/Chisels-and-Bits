package mod.chiselsandbits.client.font;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Matrix4f;
import org.jetbrains.annotations.NotNull;

public class GuiBagFontRenderer extends FontRenderer
{
    private final FontRenderer fontRenderer;

    private final int offsetX, offsetY;
    private final float scale;

    public GuiBagFontRenderer(
      final FontRenderer src,
      final int bagStackSize)
    {
        super(src.font);
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
    public int drawStringWithShadow(MatrixStack matrixStack, @NotNull String text, float x, float y, int color)
    {
        try
        {
            text = convertText(text);
            matrixStack.push();
            matrixStack.scale(scale, scale, scale);

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return fontRenderer.drawStringWithShadow(matrixStack, text, x, y, color);
        }
        finally
        {
            matrixStack.pop();
        }
    }

    @Override
    public int drawString(MatrixStack matrixStack, @NotNull String text, float x, float y, int color)
    {
        try
        {
            text = convertText(text);
            matrixStack.push();
            matrixStack.scale(scale, scale, scale);

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return fontRenderer.drawString(matrixStack, text, x, y, color);
        }
        finally
        {
            matrixStack.pop();
        }
    }

    @Override
    public int renderString(final @NotNull String text, float x, float y, final int color, final @NotNull Matrix4f matrix, final boolean dropShadow, final boolean p_228078_7_)
    {
        final MatrixStack stack = new MatrixStack();
        final Matrix4f original = new Matrix4f(matrix);

        try
        {
            stack.getLast().getMatrix().mul(matrix);
            stack.scale(scale, scale, scale);

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return super.renderString(text, x, y, color, stack.getLast().getMatrix(), dropShadow, p_228078_7_);
        }
        finally
        {
            matrix.set(original);
        }
    }

    @Override
    public int renderString(
      final @NotNull String text,
      float x,
      float y,
      final int color,
      final boolean dropShadow,
      final @NotNull Matrix4f matrix,
      final @NotNull IRenderTypeBuffer buffer,
      final boolean transparentIn,
      final int colorBackgroundIn,
      final int packedLight)
    {
        final MatrixStack stack = new MatrixStack();
        final Matrix4f original = new Matrix4f(matrix);

        try
        {
            stack.getLast().getMatrix().mul(matrix);
            stack.scale(scale, scale, scale);

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return super.renderString(text, x, y, color, dropShadow, stack.getLast().getMatrix(), buffer, transparentIn, colorBackgroundIn, packedLight);
        }
        finally
        {
            matrix.set(original);
        }
    }

    @Override
    public int getStringWidth(
      @NotNull String text)
    {
        text = convertText(text);
        return fontRenderer.getStringWidth(text);
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
