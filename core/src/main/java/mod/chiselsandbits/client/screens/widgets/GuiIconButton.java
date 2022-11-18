package mod.chiselsandbits.client.screens.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.client.screen.widget.AbstractChiselsAndBitsButton;
import mod.chiselsandbits.client.icon.IconManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiIconButton extends AbstractChiselsAndBitsButton
{
    public static final int SIZE = 20;
    TextureAtlasSprite icon;

	public GuiIconButton(
			final int x,
			final int y,
			final TextureAtlasSprite icon,
            Button.OnPress pressedAction,
            Button.OnTooltip tooltip)
	{
		super( x, y, SIZE, SIZE, Component.empty(), pressedAction, tooltip);
		this.icon = icon;
	}

    public GuiIconButton(
      final int x, final int y,
      final Component narration,
      final TextureAtlasSprite icon,
      final OnPress pressable)
    {
        super(x, y, SIZE, SIZE, narration, pressable);
        this.icon = icon;
    }

    public GuiIconButton(
      final int x, final int y,
      final Component narration,
      final TextureAtlasSprite icon,
      final OnPress pressable,
      final OnTooltip tooltip)
    {
        super(x, y, SIZE, SIZE, narration, pressable, tooltip);
        this.icon = icon;
    }

    @Override
    public void renderButton(final @NotNull PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(matrixStack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(matrixStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);

        IconManager.getInstance().bindTexture();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        blit(matrixStack, x + 2, y + 2, 0, 16,16, icon);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);

        if (this.isHovered) {
            this.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
}
