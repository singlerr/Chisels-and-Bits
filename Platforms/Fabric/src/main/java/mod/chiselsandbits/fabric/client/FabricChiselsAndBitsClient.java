package mod.chiselsandbits.fabric.client;

import mod.chiselsandbits.client.icon.IconManager;
import mod.chiselsandbits.client.logic.*;
import mod.chiselsandbits.client.model.loader.BitBlockModelLoader;
import mod.chiselsandbits.client.model.loader.ChiseledBlockModelLoader;
import mod.chiselsandbits.client.model.loader.InteractableModelLoader;
import mod.chiselsandbits.client.registrars.ModBESR;
import mod.chiselsandbits.client.registrars.ModColors;
import mod.chiselsandbits.client.registrars.ModISTER;
import mod.chiselsandbits.client.registrars.ModRenderLayers;
import mod.chiselsandbits.client.reloading.ClientResourceReloadingManager;
import mod.chiselsandbits.client.screens.BitBagScreen;
import mod.chiselsandbits.client.screens.ChiseledPrinterScreen;
import mod.chiselsandbits.client.screens.ModificationTableScreen;
import mod.chiselsandbits.client.time.TickHandler;
import mod.chiselsandbits.fabric.platform.client.rendering.model.loader.FabricPlatformModelLoaderPlatformDelegate;
import mod.chiselsandbits.keys.KeyBindingManager;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModContainerTypes;
import mod.chiselsandbits.registrars.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class FabricChiselsAndBitsClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        setupEvents();
        onInitialize();
        onModelRegistry();
        setupColors();
    }

    private static void onModelRegistry()
    {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(manager -> new FabricPlatformModelLoaderPlatformDelegate<>(
          new ResourceLocation(Constants.MOD_ID, "chiseled_block"),
          ChiseledBlockModelLoader.getInstance()
        ));

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(manager -> new FabricPlatformModelLoaderPlatformDelegate<>(
          new ResourceLocation(Constants.MOD_ID, "bit"),
          BitBlockModelLoader.getInstance()
        ));

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(manager -> new FabricPlatformModelLoaderPlatformDelegate<>(
          new ResourceLocation(Constants.INTERACTABLE_MODEL_LOADER),
          new InteractableModelLoader()
        ));
    }

    private static void onInitialize()
    {
        ClientResourceReloadingManager.setup();
        KeyBindingManager.getInstance().onModInitialization();

        ModRenderLayers.onClientInit();
        ModBESR.onClientInit();

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

        ItemProperties.register(ModItems.MEASURING_TAPE.get(), new ResourceLocation(Constants.MOD_ID, "is_measuring"), (stack, clientWorld, livingEntity, value) -> {
            if (stack.getItem() != ModItems.MEASURING_TAPE.get())
                return 0;

            return ModItems.MEASURING_TAPE.get().getStart(stack).isPresent() ? 1 : 0;
        });

        ModISTER.onClientInit();
    }

    private static void setupEvents()
    {
        ClientTickEvents.START_CLIENT_TICK.register(minecraft -> {
            ToolNameHighlightTickHandler.handleClientTickForMagnifyingGlass();
            KeyBindingManager.getInstance().handleKeyPresses();
            TickHandler.onClientTick();
        });

        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context
          , hitResult) -> !SelectedObjectHighlightHandler.onDrawHighlight());

        HudRenderCallback.EVENT.register((poseStack, v) -> SlotOverlayRenderHandler.renderSlotOverlays(poseStack));

        WorldRenderEvents.END.register(context -> {
            SelectedObjectRenderHandler.renderCustomWorldHighlight(
              context.worldRenderer(),
              context.matrixStack(),
              context.tickDelta(),
              context.projectionMatrix(),
              context.limitTime()
            );

            MeasurementsRenderHandler.renderMeasurements(context.matrixStack());

            MultiStateBlockPreviewRenderHandler.renderMultiStateBlockPreview(context.matrixStack());
        });
    }

    private static void setupColors()
    {
        ModColors.onBlockColorHandler();
        ModColors.onItemColorHandler();
    }
}
