package mod.chiselsandbits.client.screens.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.client.icon.IconManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

public class GuiIconButton extends Button
{
	TextureAtlasSprite icon;

	public GuiIconButton(
			final int x,
			final int y,
			final TextureAtlasSprite icon,
            Button.IPressable pressedAction,
            Button.ITooltip tooltip)
	{
		super( x, y, 18, 18, new StringTextComponent(""), pressedAction, tooltip);
		this.icon = icon;
	}

    @Override
    public void renderButton(final @NotNull MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        IconManager.getInstance().bindTexture();
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        blit(matrixStack, x + 1, y + 1, 0, 16,16, icon);
        Minecraft.getInstance().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
    }
}
