package mod.chiselsandbits.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.client.tool.mode.icon.SelectedToolModeRendererRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

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

    public void renderSlot(final int xOffset, final int yOffSet, final GuiGraphics graphics, final ItemStack stack)
    {
        graphics.pose().pushPose();
        graphics.pose().translate(xOffset, yOffSet, 100);
        graphics.pose().pushPose();

        SelectedToolModeRendererRegistry.getInstance().getCurrent()
                                                        .render(graphics, stack);

        graphics.pose().popPose();
        graphics.pose().popPose();
    }
}
