package mod.chiselsandbits.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import mod.chiselsandbits.client.tool.mode.icon.SelectedToolModeRendererRegistry;
import net.minecraft.item.ItemStack;

public class SlotOverlayRenderManager
{
    private static final SlotOverlayRenderManager INSTANCE = new SlotOverlayRenderManager();

    public static SlotOverlayRenderManager getInstance()
    {
        return INSTANCE;
    }

    private SlotOverlayRenderManager()
    {
    }

    public void renderSlot(final int xOffset, final int yOffSet, final MatrixStack matrixStack, final ItemStack stack)
    {
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffSet, 0);
        matrixStack.pushPose();

        SelectedToolModeRendererRegistry.getInstance().getCurrent()
                                                        .render(matrixStack, stack);

        matrixStack.popPose();
        matrixStack.popPose();
    }
}
