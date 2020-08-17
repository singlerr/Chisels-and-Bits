package mod.chiselsandbits.bitbag;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;

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
    public void renderButton(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        Minecraft.getInstance().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        blit(matrixStack, x + 1, y + 1, 0, 16,16, icon);
    }
}
