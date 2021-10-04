package mod.chiselsandbits.client.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.client.icon.IconManager;
import mod.chiselsandbits.client.icon.IconSpriteUploader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public class GuiIconButton extends Button
{
	TextureAtlasSprite icon;

	public GuiIconButton(
			final int x,
			final int y,
			final TextureAtlasSprite icon,
            Button.OnPress pressedAction,
            Button.OnTooltip tooltip)
	{
		super( x, y, 18, 18, new TextComponent(""), pressedAction, tooltip);
		this.icon = icon;
	}

    @Override
    public void renderButton(final @NotNull PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, IconSpriteUploader.TEXTURE_MAP_NAME);
        blit(matrixStack, x + 1, y + 1, 0, 16,16, icon);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
    }
}
