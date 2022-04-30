package mod.chiselsandbits.forge.events;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.logic.BitStackPickupHandler;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityItemPickupEventHandler
{

    @SubscribeEvent
    public static void pickupItems(
      final EntityItemPickupEvent event)
    {
        event.setCanceled(BitStackPickupHandler.pickupItems(
          event.getItem(),
          event.getPlayer()
        ));
    }
}