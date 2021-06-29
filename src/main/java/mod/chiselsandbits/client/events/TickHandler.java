package mod.chiselsandbits.client.events;

import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickHandler
{

    private static long clientTicks = 0;

    private static long nonePausedTicks = 0;

    @SubscribeEvent
    public static void onTickClientTick(final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            clientTicks++;
            if (!Minecraft.getInstance().isGamePaused()) {
                nonePausedTicks++;
            }
        }
    }

    public static long getClientTicks()
    {
        return clientTicks;
    }

    public static long getNonePausedTicks()
    {
        return nonePausedTicks;
    }
}
