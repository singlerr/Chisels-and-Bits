package mod.chiselsandbits.client.screens.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.client.screen.widget.AbstractChiselsAndBitsButton;
import mod.chiselsandbits.client.icon.IconManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

public class GuiIconButton extends AbstractChiselsAndBitsButton
{
    public static final int SIZE = 20;
    TextureAtlasSprite icon;

	public GuiIconButton(
			final int x,
			final int y,
			final TextureAtlasSprite icon,
            Button.IPressable pressedAction,
            Button.ITooltip tooltip)
	{
		super( x, y, SIZE, SIZE, new StringTextComponent(""), pressedAction, tooltip);
		this.icon = icon;
	}

    public GuiIconButton(
      final int x, final int y,
      final ITextComponent narration,
      final TextureAtlasSprite icon,
      final IPressable pressable)
    {
        super(x, y, SIZE, SIZE, narration, pressable);
        this.icon = icon;
    }

    public GuiIconButton(
      final int x, final int y,
      final ITextComponent narration,
      final TextureAtlasSprite icon,
      final IPressable pressable,
      final ITooltip tooltip)
    {
        super(x, y, SIZE, SIZE, narration, pressable, tooltip);
        this.icon = icon;
    }

    @Override
    public void renderButton(final @NotNull MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        RenderSystem.enableTexture();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(matrixStack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(matrixStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);

        IconManager.getInstance().bindTexture();
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        blit(matrixStack, x + 2, y + 2, 0, 16,16, icon);
        minecraft.getTextureManager().bind(WIDGETS_LOCATION);

        if (this.isHovered()) {
            this.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
}
