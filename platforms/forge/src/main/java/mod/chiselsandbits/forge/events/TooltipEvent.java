package mod.chiselsandbits.forge.events;

import mod.chiselsandbits.logic.MagnifyingGlassTooltipHandler;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipEvent
{

    @SubscribeEvent
    public static void onItemTooltip(final ItemTooltipEvent event)
    {
        MagnifyingGlassTooltipHandler.onItemTooltip(
          event.getItemStack(),
          event.getToolTip()
        );
    }
}
