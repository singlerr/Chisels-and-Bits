package mod.chiselsandbits.events;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.chiseling.ChiselingManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ServerAboutToStartEventHandler
{

    @SubscribeEvent
    public static void onFMLServerAboutToStart(final FMLServerAboutToStartEvent event)
    {
        ChiselingManager.getInstance().onServerStarting();
    }
}
