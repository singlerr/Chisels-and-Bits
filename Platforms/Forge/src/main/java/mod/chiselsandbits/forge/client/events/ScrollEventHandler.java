package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.logic.ScrollBasedModeChangeHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScrollEventHandler
{

    @SubscribeEvent
    public static void onScroll(final InputEvent.MouseScrollEvent event) {
        event.setCanceled(ScrollBasedModeChangeHandler.onScroll(event.getScrollDelta()));
    }
}
