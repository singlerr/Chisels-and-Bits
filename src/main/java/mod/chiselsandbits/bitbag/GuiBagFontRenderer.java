package mod.chiselsandbits.bitbag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiBagFontRenderer extends FontRenderer
{
	FontRenderer talkto;

	public GuiBagFontRenderer(
			final FontRenderer src )
	{
		super( Minecraft.getMinecraft().gameSettings, new ResourceLocation( "textures/font/ascii.png" ), Minecraft.getMinecraft().getTextureManager(), false );
		talkto = src;
	}

	@Override
	public int getStringWidth(
			final String text )
	{
		return talkto.getStringWidth( text );
	}

	@Override
	public int renderString(
			final String text,
			float x,
			float y,
			final int color,
			final boolean dropShadow )
	{
		try
		{
			GlStateManager.pushMatrix();
			GlStateManager.scale( 0.75, 0.75, 0.75 );
			x /= 0.75;
			y /= 0.75;
			x += 3;
			y += 2;
			return talkto.renderString(
					text,
					x,
					y,
					color,
					dropShadow );
		}
		finally
		{
			GlStateManager.popMatrix();
		}
	}
}
