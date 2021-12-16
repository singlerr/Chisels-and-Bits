package mod.chiselsandbits.forge.events;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.logic.ServerStartHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ServerAboutToStartEventHandler
{

    @SubscribeEvent
    public static void onFMLServerAboutToStart(final ServerAboutToStartEvent event)
    {
        ServerStartHandler.onServerStart();
    }
}
