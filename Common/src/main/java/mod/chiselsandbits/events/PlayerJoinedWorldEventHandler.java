package mod.chiselsandbits.events;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.chiseling.ChiselingManager;
import mod.chiselsandbits.measures.MeasuringManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
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

        ChiselingManager.getInstance().resetLastChiselCountdown((Player) event.getEntity());
    }
}
