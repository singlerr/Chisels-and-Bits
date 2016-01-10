package mod.chiselsandbits.bitbag;

import mod.chiselsandbits.ChiselsAndBits;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
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

	@Override
	public void drawScreen(
			final int mouseX,
			final int mouseY,
			final float partialTicks )
	{
		super.drawScreen( mouseX, mouseY, partialTicks );

		drawDefaultBackground();
		final int i = guiLeft;
		final int j = guiTop;
		drawGuiContainerBackgroundLayer( partialTicks, mouseX, mouseY );
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		super.drawScreen( mouseX, mouseY, partialTicks );
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translate( i, j, 0.0F );
		GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
		GlStateManager.enableRescaleNormal();

		final int k = 240;
		final int l = 240;
		OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, k / 1.0F, l / 1.0F );
		GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );

		for ( int i1 = 0; i1 < ( (BagContainer) inventorySlots ).customSlots.size(); ++i1 )
		{
			final Slot slot = ( (BagContainer) inventorySlots ).customSlots.get( i1 );
			drawSlot( slot );

			if ( isMouseOverSlot( slot, mouseX, mouseY ) && slot.canBeHovered() )
			{
				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				final int j1 = slot.xDisplayPosition;
				final int k1 = slot.yDisplayPosition;
				GlStateManager.colorMask( true, true, true, false );
				drawGradientRect( j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433 );
				GlStateManager.colorMask( true, true, true, true );
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
			}
		}

		RenderHelper.disableStandardItemLighting();
		drawGuiContainerForegroundLayer( mouseX, mouseY );
		RenderHelper.enableGUIStandardItemLighting();

		GlStateManager.popMatrix();
	}

}
