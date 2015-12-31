package mod.chiselsandbits.bitbag;

import mod.chiselsandbits.ChiselsAndBits;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BagGui extends GuiContainer
{

	private static final ResourceLocation BAG_GUI_TEXTURE = new ResourceLocation( ChiselsAndBits.MODID, "textures/gui/container/bitbag.png" );

	public BagGui(
			final EntityPlayer player,
			final World world,
			final int x,
			final int y,
			final int z )
	{
		super( new BagContainer( player, world, x, y, z ) );

		allowUserInput = false;
		ySize = 239;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(
			final int mouseX,
			final int mouseY )
	{
		fontRendererObj.drawString( ChiselsAndBits.getItems().itemBitBag.getItemStackDisplayName( null ), 8, 6, 4210752 );
		fontRendererObj.drawString( I18n.format( "container.inventory", new Object[0] ), 8, ySize - 93, 4210752 );
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(
			final float partialTicks,
			final int mouseX,
			final int mouseY )
	{
		GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
		mc.getTextureManager().bindTexture( BAG_GUI_TEXTURE );
		final int k = ( width - xSize ) / 2;
		final int l = ( height - ySize ) / 2;
		this.drawTexturedModalRect( k, l, 0, 0, xSize, ySize );
	}

}
