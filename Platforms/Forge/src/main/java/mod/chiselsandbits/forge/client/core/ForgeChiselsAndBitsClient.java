package mod.chiselsandbits.forge.client.core;

import mod.chiselsandbits.client.core.ChiselsAndBitsClient;
import mod.chiselsandbits.forge.platform.client.model.loader.ForgePlatformModelLoaderPlatformDelegate;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.model.loader.BitBlockModelLoader;
import mod.chiselsandbits.client.model.loader.ChiseledBlockModelLoader;
import mod.chiselsandbits.client.model.loader.InteractableModelLoader;
import mod.chiselsandbits.client.registrars.ModBESR;
import mod.chiselsandbits.client.registrars.ModRenderLayers;
import mod.chiselsandbits.keys.KeyBindingManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.BiConsumer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeChiselsAndBitsClient
{

    private static ChiselsAndBitsClient instance;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onModelRegistry(final ModelRegistryEvent event)
    {
        ForgeChiselsAndBitsClient.onModelRegistry(
            ModelLoaderRegistry::registerLoader
        );
    }

    @OnlyIn(Dist.CLIENT)
    public static void onModelRegistry(final BiConsumer<ResourceLocation, IModelLoader<?>> registrar)
    {
        registrar.accept(
          new ResourceLocation(Constants.MOD_ID, "chiseled_block"),
          new ForgePlatformModelLoaderPlatformDelegate<>(ChiseledBlockModelLoader.getInstance())
        );
        registrar.accept(
          new ResourceLocation(Constants.MOD_ID, "bit"),
          new ForgePlatformModelLoaderPlatformDelegate<>(BitBlockModelLoader.getInstance())
        );
        registrar.accept(
          new ResourceLocation(Constants.INTERACTABLE_MODEL_LOADER),
          new ForgePlatformModelLoaderPlatformDelegate<>(new InteractableModelLoader())
        );
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onInitialize(final FMLClientSetupEvent clientSetupEvent)
    {
        instance = new ChiselsAndBitsClient();

        KeyBindingManager.getInstance().onModInitialization();

        ModRenderLayers.onClientInit();
        ModBESR.onClientInit();
    }
}
