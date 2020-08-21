package mod.chiselsandbits.bitbag;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;

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
