package mod.chiselsandbits.station;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChiselStationScreen extends ContainerScreen<ChiselStationContainer>
{

    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(Constants.MOD_ID, "textures/gui/container/chisel_station.png");

    public ChiselStationScreen(final ChiselStationContainer screenContainer, final PlayerInventory inv, final ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void init()
    {
        this.xSize = 176;
        this.ySize = 166;

        super.init();

        this.titleX = (this.xSize - this.font.getStringPropertyWidth(this.title)) / 2;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(@NotNull final MatrixStack matrixStack, final float partialTicks, final int x, final int y)
    {
        renderBackground(matrixStack);

        //noinspection deprecation Required.
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft).getTextureManager().bindTexture(GUI_TEXTURES);
        this.blit(matrixStack, this.guiLeft, this.guiTop,0, 0, this.xSize, this.ySize);

        if (this.container.getToolStack().isEmpty())
            return;

        this.itemRenderer.renderItemAndEffectIntoGUI(Objects.requireNonNull(this.minecraft.player), this.container.getToolStack(), this.guiLeft + 81, this.guiTop + 47);

        Objects.requireNonNull(this.minecraft).getTextureManager().bindTexture(GUI_TEXTURES);
        int scaledProgress = this.container.getChiselProgressionScaled();
        matrixStack.push();
        matrixStack.translate(0,0,400);
        this.blit(matrixStack, this.guiLeft + 73 + 10 + scaledProgress, this.guiTop + 49, this.xSize + scaledProgress, 0, 16 - scaledProgress, 16);
        matrixStack.pop();

        //
        //
    }
}
