package mod.chiselsandbits.client.tool.mode.icon;

import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.client.tool.mode.icon.ISelectedToolModeIconRenderer;
import mod.chiselsandbits.api.item.withmode.IRenderableMode;
import mod.chiselsandbits.api.item.withmode.IToolMode;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RootGroupTopLeftSelectedToolModeIconRenderer implements ISelectedToolModeIconRenderer
{
    static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "group");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final ItemStack stack)
    {
        if (!(stack.getItem() instanceof final IWithModeItem<?> modeItem))
            return;

        final IToolMode<?> mode = modeItem.getMode(stack);
        final IRenderableMode renderableMode = getRootRenderableMode(mode);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(11, 1, 0);
        guiGraphics.pose().scale(1/3f, 1/3f, 1);
        guiGraphics.pose().pushPose();

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(
          (float) renderableMode.getColorVector().x(),
          (float) renderableMode.getColorVector().y(),
          (float) renderableMode.getColorVector().z(),
          (float) renderableMode.getAlphaChannel()
        );
        RenderSystem.setShaderTexture(0, mode.getIcon());
        guiGraphics.blit(mode.getIcon(), 0, 0, 16,16, 0, 0, 18, 18, 18, 18);

        guiGraphics.pose().popPose();
        guiGraphics.pose().popPose();
    }

    private IRenderableMode getRootRenderableMode(final IRenderableMode mode) {
        if (mode instanceof IToolMode && ((IToolMode<?>) mode).getGroup().isPresent())
        {
            return ((IToolMode<?>) mode).getGroup().get();
        }

        return mode;
    }
}
