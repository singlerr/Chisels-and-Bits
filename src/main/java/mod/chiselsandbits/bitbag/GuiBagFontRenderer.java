package mod.chiselsandbits.bitbag;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Matrix4f;

public class GuiBagFontRenderer extends FontRenderer
{
	FontRenderer talkto;

	int offsetX, offsetY;
	float scale;

	public GuiBagFontRenderer(
			final FontRenderer src,
			final int bagStackSize )
	{
		super(src.font);
		talkto = src;

		if ( bagStackSize < 100 )
		{
			scale = 1f;
		}
		else if ( bagStackSize >= 100 )
		{
			scale = 0.75f;
			offsetX = 3;
			offsetY = 2;
		}
	}

	@Override
	public int getStringWidth(
			String text )
	{
		text = convertText( text );
		return talkto.getStringWidth( text );
	}

    @Override
    public int renderString(final String text, float x, float y, final int color, final Matrix4f matrix, final boolean dropShadow, final boolean p_228078_7_)
    {
        final MatrixStack stack = new MatrixStack();
        final Matrix4f original = new Matrix4f(matrix);

        try {
            stack.getLast().getMatrix().mul(matrix);
            stack.scale( scale, scale, scale );

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
      final String text,
      float x,
      float y,
      final int color,
      final boolean dropShadow,
      final Matrix4f matrix,
      final IRenderTypeBuffer buffer,
      final boolean transparentIn,
      final int colorBackgroundIn,
      final int packedLight)
    {
        final MatrixStack stack = new MatrixStack();
        final Matrix4f original = new Matrix4f(matrix);

        try {
            stack.getLast().getMatrix().mul(matrix);
            stack.scale( scale, scale, scale );

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
    public int drawString(MatrixStack matrixStack, String text, float x, float y, int color)
    {
        try
        {
            text = convertText( text );
            matrixStack.push();
            matrixStack.scale( scale, scale, scale );

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return talkto.drawString(matrixStack, text, x, y, color );
        }
        finally
        {
            matrixStack.pop();
        }
    }

    @Override
    public int drawStringWithShadow(MatrixStack matrixStack, String text, float x, float y, int color)
    {
        try
        {
            text = convertText( text );
            matrixStack.push();
            matrixStack.scale( scale, scale, scale );

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return talkto.drawStringWithShadow(matrixStack, text, x, y, color );
        }
        finally
        {
            matrixStack.pop();
        }
    }

    private String convertText(
			final String text )
	{
		try
		{
			final int value = Integer.parseInt( text );

			if ( value >= 1000 )
			{
				return value / 1000 + "k";
			}

			return text;
		}
		catch ( final NumberFormatException e )
		{
			return text;
		}
	}
}
