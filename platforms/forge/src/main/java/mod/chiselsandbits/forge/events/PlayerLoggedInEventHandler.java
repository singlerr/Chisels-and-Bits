package mod.chiselsandbits.forge.events;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.logic.ChiselingManagerCountDownResetHandler;
import mod.chiselsandbits.logic.MeasuringSynchronisationHandler;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerLoggedInEventHandler
{
    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event)
    {
        MeasuringSynchronisationHandler.syncToAll();
        ChiselingManagerCountDownResetHandler.doResetFor(event.getPlayer());
    }
}
