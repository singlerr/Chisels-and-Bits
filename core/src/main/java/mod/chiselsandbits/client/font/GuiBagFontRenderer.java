package mod.chiselsandbits.client.font;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class GuiBagFontRenderer extends Font
{
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    private final Font fontRenderer;

    private final int offsetX, offsetY;
    private final float scale;

    public GuiBagFontRenderer(
      final Font src,
      final int bagStackSize)
    {
        super(src.fonts, true);
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
            stack.last().pose().mul(matrix);
            stack.scale(scale, scale, scale);

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return super.drawInternal(text, x, y, color, stack.last().pose(), dropShadow, p_228078_7_);
        }
        finally
        {
            matrix.identity();
            matrix.mul(original);
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
            stack.last().pose().mul(matrix);
            stack.scale(scale, scale, scale);

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return super.drawInBatch(convertText(text), x, y, color, dropShadow, stack.last().pose(), buffer, transparentIn, colorBackgroundIn, packedLight);
        }
        finally
        {
            matrix.identity();
            matrix.mul(original);
        }
    }

    @Override
    public int width(
      @NotNull String text)
    {
        text = convertText(text);
        return fontRenderer.width(text);
    }



    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    private String convertText(
      final String text)
    {
        try
        {
            final long value = Long.parseLong(text);
            return format(value);
        }
        catch (final NumberFormatException e)
        {
            return text;
        }
    }
}
