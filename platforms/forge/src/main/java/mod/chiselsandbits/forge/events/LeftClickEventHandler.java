package mod.chiselsandbits.forge.events;

import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.logic.LeftClickInteractionHandler;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LeftClickEventHandler
{

    @SubscribeEvent
    public static void onPlayerInteractLeftClickBlock(final PlayerInteractEvent.LeftClickBlock event)
    {
        final ClickProcessingState result = LeftClickInteractionHandler.leftClickOnBlock(
          event.getPlayer(),
          event.getHand(),
          event.getItemStack(),
          event.getPos(),
          event.getFace(),
          event.isCanceled(),
          mapResult(event.getUseItem())
        );

        if (result.shouldCancel())
            event.setCanceled(true);

        event.setUseItem(mapResult(result.getNextState()));
    }

    private static ClickProcessingState.ProcessingResult mapResult(
      final Event.Result result)
    {
        return switch (result)
                 {
                     case DENY -> ClickProcessingState.ProcessingResult.DENY;
                     case DEFAULT -> ClickProcessingState.ProcessingResult.DEFAULT;
                     case ALLOW -> ClickProcessingState.ProcessingResult.ALLOW;
                 };
    }

    private static Event.Result mapResult(
      final ClickProcessingState.ProcessingResult processingResult
    ) {
        return switch (processingResult)
                 {
                     case DENY -> Event.Result.DENY;
                     case DEFAULT -> Event.Result.DEFAULT;
                     case ALLOW -> Event.Result.ALLOW;
                 };
    }
}
