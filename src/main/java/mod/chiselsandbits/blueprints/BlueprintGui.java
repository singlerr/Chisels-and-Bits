package mod.chiselsandbits.blueprints;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JFileChooser;

import mod.chiselsandbits.blueprints.BlueprintData.EnumLoadState;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.DeprecationHelper;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketBlueprintSet;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
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

		buttonList.add( url = new GuiButton( 1, guiLeft + 10, guiTop + 35, w, 20, DeprecationHelper.translateToLocal( "mod.chiselsandbits.blueprint.url" ) ) );
		buttonList.add( local = new GuiButton( 1, guiLeft + 10, guiTop + 60, w, 20, DeprecationHelper.translateToLocal( "mod.chiselsandbits.blueprint.local" ) ) );
		buttonList.add( create = new GuiButton( 1, guiLeft + 10, guiTop + 85, w, 20, DeprecationHelper.translateToLocal( "mod.chiselsandbits.blueprint.create" ) ) );
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
		final BlueprintContainer c = (BlueprintContainer) inventorySlots;
		if ( button == local )
		{
			c.thePlayer.closeScreen();
			final Thread picker = new Thread( new Runnable() {

				@Override
				public void run()
				{
					final JFileChooser fc = new JFileChooser();
					if ( fc.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION )
					{
						final BlueprintData dat = new BlueprintData( null );
						try
						{
							dat.setLocalSource( fc.getSelectedFile().getPath() );
							dat.loadData( new FileInputStream( fc.getSelectedFile() ) );
							if ( dat.getState() == EnumLoadState.LOADED )
							{
								final PacketBlueprintSet packet = new PacketBlueprintSet();
								packet.setFrom( getBlueprintContainer().bluePrintSlot, dat );
								NetworkRouter.instance.sendToServer( packet );
							}
						}
						catch ( final FileNotFoundException e )
						{
							c.thePlayer.addChatMessage( new TextComponentTranslation( LocalStrings.ShareNoFile.toString() ) );
							Log.logError( "Unable to read file into blueprint.", e );
						}
						catch ( final IOException e )
						{
							c.thePlayer.addChatMessage( new TextComponentTranslation( LocalStrings.ShareInvalidData.toString() ) );
							Log.logError( "Invalid format or file.", e );
						}
					}
				}
			} );
			picker.setName( "Load File Picker" );
			picker.start();
		}
		else if ( button == url )
		{
			final String urlText = urlField.getText();
			c.thePlayer.closeScreen();
			final Thread picker = new Thread( new Runnable() {

				@Override
				public void run()
				{
					final BlueprintData dat = new BlueprintData( null );
					final InputStream in;

					try
					{
						final URL url = new URL( urlText );
						dat.setURLSource( url );
						in = url.openStream();
					}
					catch ( final IOException e )
					{
						c.thePlayer.addChatMessage( new TextComponentTranslation( LocalStrings.ShareNoUrl.toString() ) );
						Log.logError( "Unable to read url into blueprint.", e );
						return;
					}

					try
					{
						dat.loadData( in );
						if ( dat.getState() == EnumLoadState.LOADED )
						{
							final PacketBlueprintSet packet = new PacketBlueprintSet();
							packet.setFrom( getBlueprintContainer().bluePrintSlot, dat );
							NetworkRouter.instance.sendToServer( packet );
						}
					}
					catch ( final IOException e )
					{
						c.thePlayer.addChatMessage( new TextComponentTranslation( LocalStrings.ShareInvalidData.toString() ) );
						Log.logError( "Invalid format or file.", e );
					}
				}
			} );
			picker.setName( "Downloading blueprint - " + urlText );
			picker.start();
		}
		else if ( button == create )
		{

		}
	}

}
