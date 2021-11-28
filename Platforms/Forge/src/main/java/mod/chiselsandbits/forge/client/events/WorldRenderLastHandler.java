package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.logic.MeasurementsRenderHandler;
import mod.chiselsandbits.client.logic.MultiStateBlockPreviewRenderHandler;
import mod.chiselsandbits.client.logic.SelectedObjectRenderHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WorldRenderLastHandler
{

    @SubscribeEvent
    public static void renderCustomWorldHighlight(final RenderWorldLastEvent event)
    {
        SelectedObjectRenderHandler.renderCustomWorldHighlight(
          event.getContext(),
          event.getMatrixStack(),
          event.getPartialTicks(),
          event.getProjectionMatrix(),
          event.getFinishTimeNano()
        );
    }

    @SubscribeEvent
    public static void renderMeasurements(final RenderWorldLastEvent event)
    {
        MeasurementsRenderHandler.renderMeasurements(event.getMatrixStack());
    }

    @SubscribeEvent
    public static void renderMultiStateBlockPreview(final RenderWorldLastEvent event)
    {
        MultiStateBlockPreviewRenderHandler.renderMultiStateBlockPreview(event.getMatrixStack());
    }
}
