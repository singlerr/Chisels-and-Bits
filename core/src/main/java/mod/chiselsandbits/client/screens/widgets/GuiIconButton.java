package mod.chiselsandbits.client.screens.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.client.screen.widget.AbstractChiselsAndBitsButton;
import mod.chiselsandbits.client.icon.IconManager;
import mod.chiselsandbits.client.icon.IconSpriteUploader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
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
            Tooltip tooltip)
	{
		super( x, y, SIZE, SIZE, Component.empty(), pressedAction, Button.DEFAULT_NARRATION);
		this.icon = icon;
        this.setTooltip(tooltip);
	}

    public GuiIconButton(
      final int x, final int y,
      final Component narration,
      final TextureAtlasSprite icon,
      final OnPress pressable)
    {
        super(x, y, SIZE, SIZE, narration, pressable, Button.DEFAULT_NARRATION);
        this.icon = icon;
    }

    public GuiIconButton(
      final int x, final int y,
      final Component narration,
      final TextureAtlasSprite icon,
      final OnPress pressable,
      final Tooltip tooltip)
    {
        super(x, y, SIZE, SIZE, narration, pressable, Button.DEFAULT_NARRATION);
        this.icon = icon;
        this.setTooltip(tooltip);
    }

    @Override
    public void renderWidget(final @NotNull GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        guiGraphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());

        IconManager.getInstance().bindTexture();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blit(getX() + 2, getY() + 2, 0, 16,16, icon);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
    }

    private int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 2;
        }

        return 46 + i * 20;
    }
}
