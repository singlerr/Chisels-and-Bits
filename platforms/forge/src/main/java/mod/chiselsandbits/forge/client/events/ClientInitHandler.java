package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.client.input.FrameBasedInputTracker;
import mod.chiselsandbits.client.logic.MeasurementsRenderHandler;
import mod.chiselsandbits.client.logic.MultiStateBlockPreviewRenderHandler;
import mod.chiselsandbits.client.logic.SelectedObjectRenderHandler;
import mod.chiselsandbits.client.registrars.ModISTER;
import mod.chiselsandbits.platforms.core.client.integration.IOptifineCompatibilityManager;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.screens.BitBagScreen;
import mod.chiselsandbits.client.screens.ChiseledPrinterScreen;
import mod.chiselsandbits.client.screens.ModificationTableScreen;
import mod.chiselsandbits.registrars.ModContainerTypes;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInitHandler
{

    @SubscribeEvent
    public static void onFMLClientSetup(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            MenuScreens.register(
              ModContainerTypes.BIT_BAG.get(),
              BitBagScreen::new
            );
            MenuScreens.register(
              ModContainerTypes.MODIFICATION_TABLE.get(),
              ModificationTableScreen::new
            );
            MenuScreens.register(
              ModContainerTypes.CHISELED_PRINTER_CONTAINER.get(),
              ChiseledPrinterScreen::new
            );
        });

        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.MEASURING_TAPE.get(), new ResourceLocation(Constants.MOD_ID, "is_measuring"), (stack, clientWorld, livingEntity, value) -> {
                if (stack.getItem() != ModItems.MEASURING_TAPE.get())
                    return 0;

                return ModItems.MEASURING_TAPE.get().getStart(stack).isPresent() ? 1 : 0;
            });
        });

        ModISTER.onClientInit();

        mod.chiselsandbits.client.logic.ClientInitHandler.onClientInit();

        if (IOptifineCompatibilityManager.getInstance().isInstalled()) {
            Mod.EventBusSubscriber.Bus.FORGE.bus().get().addListener((Consumer<RenderLevelLastEvent>) event1 -> {
                SelectedObjectRenderHandler.renderCustomWorldHighlight(
                  event1.getLevelRenderer(),
                  event1.getPoseStack(),
                  event1.getPartialTick()
                );

                MeasurementsRenderHandler.renderMeasurements(event1.getPoseStack());

                MultiStateBlockPreviewRenderHandler.renderMultiStateBlockPreview(event1.getPoseStack());

                FrameBasedInputTracker.getInstance().onRenderFrame();
            });
        }
    }
}
