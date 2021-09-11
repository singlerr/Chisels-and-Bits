package mod.chiselsandbits.events;

import mod.chiselsandbits.api.item.click.ILeftClickControllingItem;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.profiling.IProfiler;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.profiling.ProfilingManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LeftClickEventHandler
{

    @SubscribeEvent
    public static void onPlayerInteractLeftClickBlock(final PlayerInteractEvent.LeftClickBlock event)
    {
        final ItemStack itemStack = event.getItemStack();
        if (itemStack.getItem() instanceof ILeftClickControllingItem) {
            ProfilingManager.getInstance().withProfiler(p -> p.startSection("Left click processing"));

            final ILeftClickControllingItem leftClickControllingItem = (ILeftClickControllingItem) itemStack.getItem();

            if (!leftClickControllingItem.canUse(event.getPlayer())) {
                event.setCanceled(true);
                event.setUseItem(Event.Result.DENY);
                ProfilingManager.getInstance().withProfiler(IProfiler::endSection);
                return;
            }

            final ClickProcessingState processingState = leftClickControllingItem.handleLeftClickProcessing(
              event.getPlayer(),
              event.getHand(),
              event.getPos(),
              event.getFace(),
              new ClickProcessingState(
                event.isCanceled(),
                event.getUseItem()
              )
            );

            if (processingState.shouldCancel())
            {
                event.setCanceled(true);
            }

            event.setUseItem(processingState.getNextState());

            ProfilingManager.getInstance().withProfiler(IProfiler::endSection);
        }
    }
}
