package mod.chiselsandbits.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.config.IServerConfiguration;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.font.GuiBagFontRenderer;
import mod.chiselsandbits.client.icon.IconManager;
import mod.chiselsandbits.client.screens.widgets.GuiIconButton;
import mod.chiselsandbits.client.util.GuiUtil;
import mod.chiselsandbits.container.BagContainer;
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
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
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
        trashBtn = addRenderableWidget(new GuiIconButton(leftPos - 20, topPos, IconManager.getInstance().getTrashIcon(),
          button -> {
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
                final ClearBagGuiPacket packet = new ClearBagGuiPacket(getInHandItem());
                ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(packet);
                packet.execute(Minecraft.getInstance().player);
            }
        }, (button, poseStack, mouseX, mouseY) -> {
            if ( isValidBitItem() )
            {
                final Component msgNotConfirm = !getInHandItem().isEmpty() ? LocalStrings.TrashItem.getText( getInHandItem().getHoverName().getString() ) : LocalStrings.Trash.getText();
                final Component msgConfirm = !getInHandItem().isEmpty() ? LocalStrings.ReallyTrashItem.getText( getInHandItem().getHoverName().getString() ) : LocalStrings.ReallyTrash.getText();

                final List<Component> text = Collections.singletonList(requireConfirm ? msgNotConfirm : msgConfirm);
                GuiUtil.drawHoveringText(poseStack, text, mouseX, mouseY, width, height, -1, Minecraft.getInstance().font );
            }
            else
            {
                final List<Component> text = Collections.singletonList(LocalStrings.TrashInvalidItem.getText(getInHandItem().getHoverName().getString()));
                GuiUtil.drawHoveringText(poseStack, text, mouseX, mouseY, width, height, -1, Minecraft.getInstance().font );
            }
        }));

        addRenderableWidget(new GuiIconButton(leftPos - 20, topPos + 22, IconManager.getInstance().getSortIcon(),
          button -> {
            final SortBagGuiPacket packet = new SortBagGuiPacket();
            ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(packet);
            packet.execute(Minecraft.getInstance().player);
          },
          (button, poseStack, mouseX, mouseY) -> {
              final List<Component> text = Collections.singletonList(LocalStrings.Sort.getText());
              GuiUtil.drawHoveringText(poseStack, text, mouseX, mouseY, width, height, -1, Minecraft.getInstance().font);
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

        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(this.leftPos, this.topPos, 0.0D);
        RenderSystem.applyModelViewMatrix();
        if ( specialFontRenderer == null )
        {
            specialFontRenderer = new GuiBagFontRenderer( font, IServerConfiguration.getInstance().getBagStackSize().get() );
        }

        hoveredBitSlot = null;
        stack.pushPose();

        for ( int slotIdx = 0; slotIdx < getBagContainer().customSlots.size(); ++slotIdx )
        {
            final Slot slot = getBagContainer().customSlots.get( slotIdx );

            final Font defaultFontRenderer = font;

            try
            {
                font = specialFontRenderer;
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                renderSlot(stack, slot);
            }
            finally
            {
                font = defaultFontRenderer;
            }

            if ( isHovering( slot, mouseX, mouseY ) && slot.isActive() )
            {
                final int xDisplayPos = slot.x;
                final int yDisplayPos = slot.y;
                hoveredBitSlot = slot;

                RenderSystem.disableDepthTest();
                RenderSystem.colorMask( true, true, true, false );
                final int INNER_SLOT_SIZE = 16;
                fillGradient(stack, xDisplayPos, yDisplayPos, xDisplayPos + INNER_SLOT_SIZE, yDisplayPos + INNER_SLOT_SIZE, -2130706433, -2130706433 );
                RenderSystem.colorMask( true, true, true, true );
                RenderSystem.enableDepthTest();
            }
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        stack.popPose();

        if ( !trashBtn.isMouseOver(mouseX, mouseY) )
        {
            requireConfirm = true;
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button)
    {
        // This is what vanilla does...
        final boolean duplicateButton = button == Minecraft.getInstance().options.keyPickItem.key.getValue() + 100;

        Slot slot = hoveredSlot;
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
		return Minecraft.getInstance().player == null ? ItemStack.EMPTY : Minecraft.getInstance().player.containerMenu.getCarried();
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
