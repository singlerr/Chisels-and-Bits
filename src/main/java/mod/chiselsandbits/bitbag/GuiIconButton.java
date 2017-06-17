package mod.chiselsandbits.bitbag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

public class GuiIconButton extends GuiButton
{
	TextureAtlasSprite icon;

	public GuiIconButton(
			final int buttonId,
			final int x,
			final int y,
			final String buttonText,
			final TextureAtlasSprite icon )
	{
		super( buttonId, x, y, 18, 18, "" );
		this.icon = icon;
	}

	@Override
	public void func_191745_a(
			final Minecraft mc,
			final int mouseX,
			final int mouseY,
			final float partial )
	{
		// drawButton
		super.func_191745_a( mc, mouseX, mouseY, partial );

		mc.getTextureMapBlocks();
		mc.getTextureManager().bindTexture( TextureMap.LOCATION_BLOCKS_TEXTURE );
		GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );

		drawTexturedModalRect( xPosition + 1, yPosition + 1, icon, 16, 16 );
	}

}
