package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.client.logic.SelectedObjectHighlightHandler;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DrawHighlightHandler
{

    @SubscribeEvent
    public static void onDrawHighlight(final DrawSelectionEvent.HighlightBlock event)
    {
        event.setCanceled(SelectedObjectHighlightHandler.onDrawHighlight());
    }
}
