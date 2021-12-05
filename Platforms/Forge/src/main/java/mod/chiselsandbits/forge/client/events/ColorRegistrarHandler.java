package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.client.registrars.ModColors;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ColorRegistrarHandler
{

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void registerBlockColors(
      final ColorHandlerEvent.Block event )
    {
        ModColors.onBlockColorHandler();
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void registerItemColors(
      final ColorHandlerEvent.Item event )
    {
        ModColors.onItemColorHandler();
    }
}
