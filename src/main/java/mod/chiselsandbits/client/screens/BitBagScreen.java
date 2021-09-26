package mod.chiselsandbits.client.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.client.font.GuiBagFontRenderer;
import mod.chiselsandbits.client.screens.widgets.GuiIconButton;
import mod.chiselsandbits.client.icon.IconManager;
import mod.chiselsandbits.container.BagContainer;
import mod.chiselsandbits.container.helper.MergeSupportingHelperContainer;
import mod.chiselsandbits.inventory.wrapping.WrappingInventory;
import mod.chiselsandbits.network.packets.BagGuiPacket;
import mod.chiselsandbits.network.packets.ClearBagGuiPacket;
import mod.chiselsandbits.network.packets.SortBagGuiPacket;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fmlclient.gui.GuiUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class BitBagScreen extends AbstractContainerScreen<BagContainer>
{

	private static final ResourceLocation BAG_GUI_TEXTURE = new ResourceLocation( Constants.MOD_ID, "textures/gui/container/bitbag.png" );

    private static GuiBagFontRenderer specialFontRenderer = null;
	private        GuiIconButton      trashBtn;

    private Slot hoveredBitSlot = null;

	public BitBagScreen(
			final BagContainer container,
            final Inventory playerInventory,
            final Component title
    )
	{
		super(container, playerInventory, title);
		imageHeight = 239;
	}

    @Override
    protected void init()
    {
        super.init();
        trashBtn = addRenderableWidget(new GuiIconButton(leftPos - 18, topPos, IconManager.getInstance().getTrashIcon(), p_onPress_1_ -> {
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
                ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new ClearBagGuiPacket(getInHandItem()));
            }
        }, (p_onTooltip_1_, p_onTooltip_2_, p_onTooltip_3_, p_onTooltip_4_) -> {
            if ( isValidBitItem() )
            {
                final String msgNotConfirm = !getInHandItem().isEmpty() ? LocalStrings.TrashItem.getLocal( getInHandItem().getHoverName().getString() ) : LocalStrings.Trash.getLocal();
                final String msgConfirm = !getInHandItem().isEmpty() ? LocalStrings.ReallyTrashItem.getLocal( getInHandItem().getHoverName().getString() ) : LocalStrings.ReallyTrash.getLocal();


                final List<Component> text = Arrays
                                                    .asList( new Component[] { new TextComponent(requireConfirm ? msgNotConfirm : msgConfirm) } );
                GuiUtils.drawHoveringText(p_onTooltip_2_, text, p_onTooltip_3_, p_onTooltip_4_, width, height, -1, Minecraft.getInstance().font );
            }
            else
            {
                final List<Component> text = Arrays
                                                    .asList( new Component[] { new TextComponent(LocalStrings.TrashInvalidItem.getLocal( getInHandItem().getHoverName().getString() )) } );
                GuiUtils.drawHoveringText(p_onTooltip_2_, text, p_onTooltip_3_, p_onTooltip_4_, width, height, -1, Minecraft.getInstance().font );
            }
        }));

        addRenderableWidget(new GuiIconButton(leftPos - 18, topPos + 18, IconManager.getInstance().getSortIcon(),
          p_onPress_1_ -> ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new SortBagGuiPacket()),
          (p_onTooltip_1_, p_onTooltip_2_, p_onTooltip_3_, p_onTooltip_4_) -> {
              final List<Component> text = Arrays
                                                  .asList(new Component[] {new TextComponent(LocalStrings.Sort.getLocal())});
              GuiUtils.drawHoveringText(p_onTooltip_2_, text, p_onTooltip_3_, p_onTooltip_4_, width, height, -1, Minecraft.getInstance().font);
          }));
    }

	BagContainer getBagContainer()
	{
		return menu;
	}

    @Override
    protected boolean hasClickedOutside(final double mouseX, final double mouseY, final int guiLeftIn, final int guiTopIn, final int mouseButton)
    {
        final boolean doThrow = !dontThrow;
        if (requireConfirm && dontThrow)
            dontThrow = false;
        return doThrow && super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn, mouseButton);
    }

    @Override
    public void render(
      final @NotNull PoseStack stack,
      final int mouseX,
      final int mouseY,
      final float partialTicks )
    {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks );
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void renderBg(
      final @NotNull PoseStack stack,
      final float partialTicks,
      final int mouseX,
      final int mouseY )
    {
        final int xOffset = ( width - imageWidth ) / 2;
        final int yOffset = ( height - imageHeight ) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BAG_GUI_TEXTURE);

        this.blit(stack, xOffset, yOffset, 0, 0, imageWidth, imageHeight );

        if ( specialFontRenderer == null )
        {
            specialFontRenderer = new GuiBagFontRenderer( font, Configuration.getInstance().getServer().bagStackSize.get() );
        }

        hoveredBitSlot = null;
        for ( int slotIdx = 0; slotIdx < getBagContainer().customSlots.size(); ++slotIdx )
        {
            final Slot slot = getBagContainer().customSlots.get( slotIdx );

            final Font defaultFontRenderer = font;

            try
            {
                font = specialFontRenderer;
                stack.pushPose();
                stack.translate(leftPos, topPos, 0f);
                renderSlot(stack, slot);
                stack.popPose();
            }
            finally
            {
                font = defaultFontRenderer;
            }

            if ( isHovering( slot, mouseX, mouseY ) && slot.isActive() )
            {
                final int xDisplayPos = this.leftPos + slot.x;
                final int yDisplayPos = this.topPos + slot.y;
                hoveredBitSlot = slot;

                RenderSystem.disableDepthTest();
                RenderSystem.colorMask( true, true, true, false );
                final int INNER_SLOT_SIZE = 16;
                fillGradient(stack, xDisplayPos, yDisplayPos, xDisplayPos + INNER_SLOT_SIZE, yDisplayPos + INNER_SLOT_SIZE, -2130706433, -2130706433 );
                RenderSystem.colorMask( true, true, true, true );
                RenderSystem.enableDepthTest();
            }
        }

        if ( !trashBtn.isMouseOver(mouseX, mouseY) )
        {
            requireConfirm = true;
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button)
    {
        // This is what vanilla does...
        final boolean duplicateButton = button == Minecraft.getInstance().options.keyPickItem.getKey().getValue() + 100;

        Slot slot = getSlotUnderMouse();
        if (slot == null)
            slot = hoveredBitSlot;
        if ( slot != null && slot.container instanceof WrappingInventory && Minecraft.getInstance().player != null)
        {
            final BagGuiPacket bagGuiPacket = new BagGuiPacket(slot.index, button, duplicateButton, (Minecraft.getInstance().getWindow() != null && Screen.hasShiftDown()));
            bagGuiPacket.doAction( Minecraft.getInstance().player );

            ChiselsAndBits.getInstance().getNetworkChannel().sendToServer( bagGuiPacket );

            return true;
        }

        return super.mouseClicked( mouseX, mouseY, button );
    }


	private ItemStack getInHandItem()
	{
		return Minecraft.getInstance().player == null ? ItemStack.EMPTY : Minecraft.getInstance().player.getInventory().getSelected();
	}

	boolean requireConfirm = true;
	boolean dontThrow = false;

	private boolean isValidBitItem()
	{
		return getInHandItem().isEmpty() || getInHandItem().getItem() == ModItems.ITEM_BLOCK_BIT.get();
	}

    @Override
    protected void renderLabels(final @NotNull PoseStack matrixStack, final int x, final int y)
    {
        font.drawShadow(matrixStack, Language.getInstance().getVisualOrder(ModItems.BIT_BAG_DEFAULT.get().getName( ItemStack.EMPTY )), 8, 6, 0x404040 );
        font.draw(matrixStack, I18n.get( "container.inventory" ), 8, imageHeight - 93, 0x404040 );
    }
}
