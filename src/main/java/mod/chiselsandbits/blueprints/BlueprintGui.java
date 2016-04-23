package mod.chiselsandbits.blueprints;

import java.io.IOException;

import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class BlueprintGui extends GuiContainer
{

	private static final ResourceLocation BAG_GUI_TEXTURE = new ResourceLocation( ChiselsAndBits.MODID, "textures/gui/container/blueprint.png" );

	private GuiButton local;
	private GuiButton url;
	private GuiButton create;

	private GuiTextField urlField;

	public BlueprintGui(
			final EntityPlayer player,
			final World world,
			final int x,
			final int y,
			final int z )
	{
		super( new BlueprintContainer( player, world, x, y, z ) );

		allowUserInput = false;
		ySize = 163;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		final int w = 176 - 20;

		urlField = new GuiTextField( 0, fontRendererObj, guiLeft + 10, guiTop + 10, w, 20 );
		urlField.setTextColor( -1 );
		urlField.setDisabledTextColour( -1 );
		urlField.setEnableBackgroundDrawing( true );
		urlField.setMaxStringLength( 64 );

		buttonList.add( url = new GuiButton( 1, guiLeft + 10, guiTop + 35, w, 20, StatCollector.translateToLocal( "mod.chiselsandbits.blueprint.url" ) ) );
		buttonList.add( local = new GuiButton( 1, guiLeft + 10, guiTop + 60, w, 20, StatCollector.translateToLocal( "mod.chiselsandbits.blueprint.local" ) ) );
		buttonList.add( create = new GuiButton( 1, guiLeft + 10, guiTop + 85, w, 20, StatCollector.translateToLocal( "mod.chiselsandbits.blueprint.create" ) ) );
	}

	@Override
	protected void keyTyped(
			final char typedChar,
			final int keyCode ) throws IOException
	{
		if ( urlField.textboxKeyTyped( typedChar, keyCode ) )
		{

		}
		else
		{
			super.keyTyped( typedChar, keyCode );
		}
	}

	@Override
	protected void mouseClicked(
			final int mouseX,
			final int mouseY,
			final int mouseButton ) throws IOException
	{
		super.mouseClicked( mouseX, mouseY, mouseButton );
		urlField.mouseClicked( mouseX, mouseY, mouseButton );
	}

	@Override
	public void drawScreen(
			final int mouseX,
			final int mouseY,
			final float partialTicks )
	{
		super.drawScreen( mouseX, mouseY, partialTicks );
		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
		urlField.drawTextBox();
	}

	BlueprintContainer getBlueprintContainer()
	{
		return (BlueprintContainer) inventorySlots;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(
			final float partialTicks,
			final int mouseX,
			final int mouseY )
	{
		final int xOffset = ( width - xSize ) / 2;
		final int yOffset = ( height - ySize ) / 2;

		GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
		mc.getTextureManager().bindTexture( BAG_GUI_TEXTURE );
		this.drawTexturedModalRect( xOffset, yOffset, 0, 0, xSize, ySize );
	}

	@Override
	protected void actionPerformed(
			final GuiButton button ) throws IOException
	{
		if ( button == local )
		{

		}
		else if ( button == url )
		{

		}
		else if ( button == create )
		{

		}
	}

}
