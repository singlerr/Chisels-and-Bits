package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.time.TickHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientTickTimingEventHandler
{

    @SubscribeEvent
    public static void onTickClientTick(final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            TickHandler.onClientTick();
        }
    }
}
