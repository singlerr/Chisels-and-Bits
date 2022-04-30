package mod.chiselsandbits.forge.events;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.client.core.ChiselsAndBitsClient;
import mod.chiselsandbits.client.model.loader.BitBlockModelLoader;
import mod.chiselsandbits.client.model.loader.ChiseledBlockModelLoader;
import mod.chiselsandbits.client.model.loader.InteractableModelLoader;
import mod.chiselsandbits.client.registrars.ModBESR;
import mod.chiselsandbits.client.registrars.ModRenderLayers;
import mod.chiselsandbits.forge.platform.client.model.loader.ForgePlatformModelLoaderPlatformDelegate;
import mod.chiselsandbits.keys.KeyBindingManager;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.function.BiConsumer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class OnCommonSetup
{

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onInitialize(final FMLCommonSetupEvent commonSetupEvent)
    {
        ChiselsAndBits.getInstance().onCommonConstruction();
    }
}
