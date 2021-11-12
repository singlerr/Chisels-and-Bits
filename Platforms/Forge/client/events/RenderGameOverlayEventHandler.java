package mod.chiselsandbits.client.events;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.client.render.SlotOverlayRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RenderGameOverlayEventHandler
{

    @SubscribeEvent
    public static void onPostRenderHotBar(final RenderGameOverlayEvent.PostLayer event) {
        if (event.getOverlay() != ForgeIngameGui.HOTBAR_ELEMENT)
            return;

        int centerOfScreen = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;

        for(int slotIndex = 0; slotIndex < 9; ++slotIndex) {
            int xOffset = centerOfScreen - 90 + slotIndex * 20 + 2;
            int yOffSet = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 16 - 3;

            SlotOverlayRenderManager.getInstance().renderSlot(xOffset, yOffSet, event.getMatrixStack(), Minecraft.getInstance().player.getInventory().items.get(slotIndex));
        }
    }
}
