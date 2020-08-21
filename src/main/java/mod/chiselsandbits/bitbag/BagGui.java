package mod.chiselsandbits.bitbag;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.network.packets.PacketBagGui;
import mod.chiselsandbits.network.packets.PacketClearBagGui;
import mod.chiselsandbits.network.packets.PacketSortBagGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class BagGui extends ContainerScreen<BagContainer>
{

	private static final ResourceLocation BAG_GUI_TEXTURE = new ResourceLocation( ChiselsAndBits.MODID, "textures/gui/container/bitbag.png" );
	private static int INNER_SLOT_SIZE = 16;

	private static GuiBagFontRenderer specialFontRenderer = null;
	private GuiIconButton trashBtn;
	private GuiIconButton sortBtn;

	public BagGui(
			final BagContainer container,
            final PlayerInventory playerInventory)
	{
		super(container, playerInventory, new StringTextComponent("BitBag"));
		ySize = 239;
	}

    @Override
    protected void init()
    {
        super.init();
        addButton(new GuiIconButton(guiLeft - 18, guiTop + 0, ClientSide.trashIcon, p_onPress_1_ -> {
            if (requireConfirm)
            {
                dontThrow = true;
                if (isValidBitItem())
                {
                    requireConfirm = false;
                }
            }
            else
            {
                requireConfirm = true;
                // server side!
                ChiselsAndBits.getNetworkChannel().sendToServer(new PacketClearBagGui(getInHandItem()));
                dontThrow = false;
            }
        }, (p_onTooltip_1_, p_onTooltip_2_, p_onTooltip_3_, p_onTooltip_4_) -> {
            if ( isValidBitItem() )
            {
                final String msgNotConfirm = ModUtil.notEmpty( getInHandItem() ) ? LocalStrings.TrashItem.getLocal( getInHandItem().getDisplayName() ) : LocalStrings.Trash.getLocal();
                final String msgConfirm = ModUtil.notEmpty( getInHandItem() ) ? LocalStrings.ReallyTrashItem.getLocal( getInHandItem().getDisplayName() ) : LocalStrings.ReallyTrash.getLocal();


                final List<ITextComponent> text = Arrays
                                                    .asList( new ITextComponent[] { new StringTextComponent(requireConfirm ? msgNotConfirm : msgConfirm) } );
                renderToolTip(p_onTooltip_2_, text, p_onTooltip_3_ - guiLeft, p_onTooltip_4_ - guiTop, Minecraft.getInstance().fontRenderer );
            }
            else
            {
                final List<ITextComponent> text = Arrays
                                                    .asList( new ITextComponent[] { new StringTextComponent(LocalStrings.TrashInvalidItem.getLocal( getInHandItem().getDisplayName() )) } );
                renderToolTip(p_onTooltip_2_, text, p_onTooltip_3_ - guiLeft, p_onTooltip_4_ - guiTop, Minecraft.getInstance().fontRenderer );
            }
        }));

        addButton(new GuiIconButton(guiLeft - 18, guiTop + 18, ClientSide.sortIcon, new Button.IPressable()
        {
            @Override
            public void onPress(final Button p_onPress_1_)
            {
                ChiselsAndBits.getNetworkChannel().sendToServer(new PacketSortBagGui());
            }
        },
          (p_onTooltip_1_, p_onTooltip_2_, p_onTooltip_3_, p_onTooltip_4_) -> {
              final List<ITextComponent> text = Arrays
                                          .asList( new ITextComponent[] { new StringTextComponent(LocalStrings.Sort.getLocal()) } );
              renderToolTip(p_onTooltip_2_, text, p_onTooltip_3_ - guiLeft, p_onTooltip_4_ - guiTop, Minecraft.getInstance().fontRenderer );
          }));
    }

	BagContainer getBagContainer()
	{
		return (BagContainer) container;
	}

    @Override
    public void render(
      final MatrixStack stack,
      final int mouseX,
      final int mouseY,
      final float partialTicks )
    {
        drawDefaultBackground(stack, partialTicks, mouseX, mouseY);
        super.render(stack, mouseX, mouseY, partialTicks );
        drawGuiContainerForegroundLayer(stack, mouseX, mouseY );
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(
      final MatrixStack stack,
      final float partialTicks,
      final int mouseX,
      final int mouseY )
    {
        final int xOffset = ( width - xSize ) / 2;
        final int yOffset = ( height - ySize ) / 2;

        RenderSystem.color4f( 1.0F, 1.0F, 1.0F, 1.0F );
        Minecraft.getInstance().getTextureManager().bindTexture( BAG_GUI_TEXTURE );
        this.blit(stack, xOffset, yOffset, 0, 0, xSize, ySize );
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button)
    {
        // This is what vanilla does...
        final boolean duplicateButton = button == Minecraft.getInstance().gameSettings.keyBindPickBlock.getKey().getKeyCode() + 100;

        final Slot slot = getSlotUnderMouse();
        if ( slot != null )
        {
            final PacketBagGui bagGuiPacket = new PacketBagGui(slot.slotNumber, button, duplicateButton, ClientSide.instance.holdingShift());
            bagGuiPacket.doAction( ClientSide.instance.getPlayer() );

            ChiselsAndBits.getNetworkChannel().sendToServer( bagGuiPacket );

            return true;
        }

        return super.mouseClicked( mouseX, mouseY, button );
    }


	private ItemStack getInHandItem()
	{
		return getBagContainer().thePlayer.inventory.getItemStack();
	}

	boolean requireConfirm = true;
	boolean dontThrow = false;

	private boolean isValidBitItem()
	{
		return ModUtil.isEmpty( getInHandItem() ) || getInHandItem().getItem() == ChiselsAndBits.getItems().itemBlockBit;
	}

    protected void drawDefaultBackground(final MatrixStack matrixStack, final float partialTicks, final int x, final int y)
    {
        font.drawString(matrixStack, ChiselsAndBits.getItems().itemBitBagDefault.getItemStackDisplayName( ModUtil.getEmptyStack() ), 8, 6, 0x404040 );
        font.drawString(matrixStack, I18n.format( "container.inventory" ), 8, ySize - 93, 0x404040 );

        RenderHelper.enableStandardItemLighting();

        if ( specialFontRenderer == null )
        {
            specialFontRenderer = new GuiBagFontRenderer( font, ChiselsAndBits.getConfig().bagStackSize );
        }

        for ( int slotIdx = 0; slotIdx < getBagContainer().customSlots.size(); ++slotIdx )
        {
            final Slot slot = getBagContainer().customSlots.get( slotIdx );

            final FontRenderer defaultFontRenderer = font;

            try
            {
                font = specialFontRenderer;
                moveItems(matrixStack, slot);
            }
            finally
            {
                font = defaultFontRenderer;
            }

            if ( isSlotSelected( slot, x, y ) && slot.isEnabled() )
            {
                final int xDisplayPos = slot.xPos;
                final int yDisplayPos = slot.yPos;
                hoveredSlot = slot;

                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask( true, true, true, false );
                fill(matrixStack, xDisplayPos, yDisplayPos, xDisplayPos + INNER_SLOT_SIZE, yDisplayPos + INNER_SLOT_SIZE, 0x80FFFFFF );
                RenderSystem.colorMask( true, true, true, true );
                RenderSystem.enableLighting();
                RenderSystem.enableDepthTest();
            }
        }

        if ( !trashBtn.isMouseOver(x, y) )
        {
            requireConfirm = true;
        }
    }
}
