package mod.chiselsandbits.events;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.measures.MeasuringManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerLoggedInEventHandler
{
    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event)
    {
        MeasuringManager.getInstance().syncToAll();
    }
}
