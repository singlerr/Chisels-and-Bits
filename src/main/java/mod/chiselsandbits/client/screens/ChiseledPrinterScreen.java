package mod.chiselsandbits.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.container.ChiseledPrinterContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChiseledPrinterScreen extends ContainerScreen<ChiseledPrinterContainer>
{

    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(Constants.MOD_ID, "textures/gui/container/chisel_printer.png");

    public ChiseledPrinterScreen(final ChiseledPrinterContainer screenContainer, final PlayerInventory inv, final ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void init()
    {
        this.imageWidth = 176;
        this.imageHeight = 166;

        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(@NotNull final MatrixStack matrixStack, final float partialTicks, final int x, final int y)
    {
        renderBackground(matrixStack);

        //noinspection deprecation Required.
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft).getTextureManager().bind(GUI_TEXTURES);
        this.blit(matrixStack, this.leftPos, this.topPos,0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.getToolStack().isEmpty())
            return;

        this.itemRenderer.renderAndDecorateItem(Objects.requireNonNull(this.minecraft.player), this.menu.getToolStack(), this.leftPos + 81, this.topPos + 47);

        Objects.requireNonNull(this.minecraft).getTextureManager().bind(GUI_TEXTURES);
        int scaledProgress = this.menu.getChiselProgressionScaled();
        matrixStack.pushPose();
        matrixStack.translate(0,0,400);
        this.blit(matrixStack, this.leftPos + 73 + 10 + scaledProgress, this.topPos + 49, this.imageWidth + scaledProgress, 0, 16 - scaledProgress, 16);
        matrixStack.popPose();
    }
}