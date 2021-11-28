package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.logic.ToolNameHighlightTickHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTickHandler
{

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void handleClientTickForMagnifyingGlass(final TickEvent.ClientTickEvent event)
    {
        ToolNameHighlightTickHandler.handleClientTickForMagnifyingGlass();
    }
}
