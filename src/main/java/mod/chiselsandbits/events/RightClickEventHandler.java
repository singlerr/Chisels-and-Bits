package mod.chiselsandbits.events;

import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.click.IRightClickControllingItem;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RightClickEventHandler
{

    @SubscribeEvent
    public static void onPlayerInteractRightClickBlock(final PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getWorld().getBlockState(event.getPos()).getBlock() == ModBlocks.BIT_STORAGE.get()) {
            event.setUseBlock(Event.Result.ALLOW);
            return;
        }

        final ItemStack itemStack = event.getItemStack();
        if (itemStack.getItem() instanceof IRightClickControllingItem) {
            final IRightClickControllingItem rightClickControllingItem = (IRightClickControllingItem) itemStack.getItem();

            if (!rightClickControllingItem.canUse(event.getPlayer())) {
                event.setCanceled(true);
                event.setUseItem(Event.Result.DENY);
                return;
            }

            final ClickProcessingState processingState = rightClickControllingItem.handleRightClickProcessing(
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
        }
    }
}
