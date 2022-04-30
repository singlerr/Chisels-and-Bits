package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.logic.SlotOverlayRenderHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SlotOverlayPostLayerHandler
{

    @SubscribeEvent
    public static void onPostRenderHotBar(final RenderGameOverlayEvent.PostLayer event) {
        if (event.getOverlay() != ForgeIngameGui.HOTBAR_ELEMENT)
            return;

        SlotOverlayRenderHandler.renderSlotOverlays(event.getMatrixStack());
    }
}
