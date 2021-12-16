package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.logic.PatternContentInTooltipHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PostTextRenderTooltipHandler
{
    //TODO: REPLACE WITH VANILLA LOGIC
    //@SubscribeEvent
    //public static void onRenderTooltipPostText(final RenderTooltipEvent.PostText event)
    //{
    //    PatternContentInTooltipHandler.doRenderContent(
    //      event.getStack(),
    //      event.getX(),
    //      event.getY()
    //    );
    //}
}
