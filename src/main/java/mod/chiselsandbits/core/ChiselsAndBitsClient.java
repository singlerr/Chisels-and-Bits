package mod.chiselsandbits.core;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.client.model.loader.ChiseledBlockModelLoader;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class ChiselsAndBitsClient
{

    @OnlyIn(Dist.CLIENT)
    public static void onModelRegistry(final ModelRegistryEvent event)
    {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Constants.MOD_ID, "chiseled_block"), ChiseledBlockModelLoader.getInstance());
    }
}
