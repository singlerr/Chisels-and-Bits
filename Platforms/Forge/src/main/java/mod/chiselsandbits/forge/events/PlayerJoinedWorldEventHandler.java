package mod.chiselsandbits.forge.events;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.logic.ChiselingManagerCountDownResetHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerJoinedWorldEventHandler
{
    @SubscribeEvent
    public static void onPlayerJoinedWorld(final EntityJoinWorldEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;

        ChiselingManagerCountDownResetHandler.doResetFor((Player) event.getEntity());
    }
}
