package mod.chiselsandbits.bitbag;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.ClientSide;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketBagGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
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

	BagContainer getBagContainer()
	{
		return (BagContainer) inventorySlots;
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

	private Slot getSlotAtPosition(
			final int x,
			final int y )
	{
		for ( int i = 0; i < getBagContainer().customSlots.size(); ++i )
		{
			final Slot slot = getBagContainer().customSlots.get( i );

			if ( isMouseOverSlot( slot, x, y ) )
			{
				return slot;
			}
		}

		return null;
	}

	@Override
	protected void mouseClicked(
			final int mouseX,
			final int mouseY,
			final int mouseButton ) throws IOException
	{
		final boolean duplicateButton = mouseButton == mc.gameSettings.keyBindPickBlock.getKeyCode() + 100;

		final Slot slot = getSlotAtPosition( mouseX, mouseY );

		if ( slot != null )
		{
			final PacketBagGui bgp = new PacketBagGui();

			bgp.slotNumber = slot.slotNumber;
			bgp.mouseButton = mouseButton;
			bgp.duplicateButton = duplicateButton;
			bgp.holdingShift = Keyboard.isKeyDown( 42 ) || Keyboard.isKeyDown( 54 );

			bgp.doAction( ClientSide.instance.getPlayer() );
			NetworkRouter.instance.sendToServer( bgp );

			return;
		}

		super.mouseClicked( mouseX, mouseY, mouseButton );
	}

	@Override
	protected void drawGuiContainerForegroundLayer(
			final int mouseX,
			final int mouseY )
	{
		fontRendererObj.drawString( ChiselsAndBits.getItems().itemBitBag.getItemStackDisplayName( null ), 8, 6, 4210752 );
		fontRendererObj.drawString( I18n.format( "container.inventory", new Object[0] ), 8, ySize - 93, 4210752 );

		RenderHelper.enableGUIStandardItemLighting();

		final RenderItem originalItemRender = itemRender;

		for ( int i1 = 0; i1 < getBagContainer().customSlots.size(); ++i1 )
		{
			final Slot slot = getBagContainer().customSlots.get( i1 );

			final FontRenderer originalheight = fontRendererObj;
			fontRendererObj = new GuiBagFontRenderer( originalheight );
			drawSlot( slot );
			fontRendererObj = originalheight;

			if ( isMouseOverSlot( slot, mouseX, mouseY ) && slot.canBeHovered() )
			{
				theSlot = slot;

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
	}

}
