package mod.chiselsandbits.client.tool.mode.icon;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.client.tool.mode.icon.ISelectedToolModeIconRenderer;
import mod.chiselsandbits.api.item.withmode.IRenderableMode;
import mod.chiselsandbits.api.item.withmode.IToolMode;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RootGroupTopLeftSelectedToolModeIconRenderer implements ISelectedToolModeIconRenderer
{
    static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "group");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void render(final MatrixStack matrixStack, final ItemStack stack)
    {
        if (!(stack.getItem() instanceof IWithModeItem))
            return;

        final IWithModeItem<?> modeItem = (IWithModeItem<?>) stack.getItem();
        final IToolMode<?> mode = modeItem.getMode(stack);
        final IRenderableMode renderableMode = getRootRenderableMode(mode);

        matrixStack.pushPose();
        matrixStack.translate(11, 1, 0);
        matrixStack.scale(1/3f, 1/3f, 1);
        matrixStack.pushPose();

        RenderSystem.color4f(
          (float) renderableMode.getColorVector().x(),
          (float) renderableMode.getColorVector().y(),
          (float) renderableMode.getColorVector().z(),
          1
        );
        Minecraft.getInstance().getTextureManager().bind(renderableMode.getIcon());
        AbstractGui.blit(matrixStack, 0, 0, 16,16, 0, 0, 18, 18, 18, 18);

        matrixStack.popPose();
        matrixStack.popPose();
    }

    private IRenderableMode getRootRenderableMode(final IRenderableMode mode) {
        if (mode instanceof IToolMode && ((IToolMode<?>) mode).getGroup().isPresent())
        {
            return ((IToolMode<?>) mode).getGroup().get();
        }

        return mode;
    }
}
