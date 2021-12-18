package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.client.input.FrameBasedInputTracker;
import mod.chiselsandbits.client.logic.MeasurementsRenderHandler;
import mod.chiselsandbits.client.logic.MultiStateBlockPreviewRenderHandler;
import mod.chiselsandbits.client.logic.SelectedObjectRenderHandler;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LevelRenderLastHandler
{

    @SubscribeEvent
    public static void renderCustomWorldHighlight(final RenderLevelLastEvent event)
    {
        SelectedObjectRenderHandler.renderCustomWorldHighlight(
          event.getLevelRenderer(),
          event.getPoseStack(),
          event.getPartialTick(),
          event.getProjectionMatrix(),
          event.getStartNanos()
        );
    }

    @SubscribeEvent
    public static void renderMeasurements(final RenderLevelLastEvent event)
    {
        MeasurementsRenderHandler.renderMeasurements(event.getPoseStack());
    }

    @SubscribeEvent
    public static void renderMultiStateBlockPreview(final RenderLevelLastEvent event)
    {
        MultiStateBlockPreviewRenderHandler.renderMultiStateBlockPreview(event.getPoseStack());
    }

    @SubscribeEvent
    public static void processAdditionalInputTracking(final RenderLevelLastEvent event)
    {
        FrameBasedInputTracker.getInstance().onRenderFrame();
    }
}
