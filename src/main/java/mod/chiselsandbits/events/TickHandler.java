package mod.chiselsandbits.events;

import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickHandler
{

    private static long clientTicks = 0;

    @SubscribeEvent
    public static void onTickClientTick(final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
            clientTicks++;
    }

    public static long getClientTicks()
    {
        return clientTicks;
    }
}
