package mod.chiselsandbits.forge.client.core;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.model.loader.BitBlockModelLoader;
import mod.chiselsandbits.client.model.loader.ChiseledBlockModelLoader;
import mod.chiselsandbits.client.model.loader.InteractableModelLoader;
import mod.chiselsandbits.client.registrars.ModBESR;
import mod.chiselsandbits.client.registrars.ModRenderLayers;
import mod.chiselsandbits.keys.KeyBindingManager;
import mod.chiselsandbits.platforms.core.client.models.loaders.IModelSpecification;
import mod.chiselsandbits.platforms.core.client.models.loaders.IModelSpecificationLoader;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiselsAndBitsClient
{

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onModelRegistry(final ModelRegistryEvent event)
    {
        ChiselsAndBitsClient.onModelRegistry(
          ModelLoaderRegistry::registerLoader
        );
    }

    @OnlyIn(Dist.CLIENT)
    public static void onModelRegistry(final BiConsumer<ResourceLocation, IModelLoader<?>> registrar)
    {
        registrar.accept(
          new ResourceLocation(Constants.MOD_ID, "chiseled_block"),
          new PlatformModelLoaderDelegate<>(ChiseledBlockModelLoader.getInstance())
        );
        registrar.accept(
          new ResourceLocation(Constants.MOD_ID, "bit"),
          new PlatformModelLoaderDelegate<>(BitBlockModelLoader.getInstance())
        );
        registrar.accept(
          new ResourceLocation(Constants.INTERACTABLE_MODEL_LOADER),
          new PlatformModelLoaderDelegate<>(new InteractableModelLoader())
        );
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onInitialize(final FMLClientSetupEvent clientSetupEvent)
    {
        KeyBindingManager.getInstance().onModInitialization();

        ModRenderLayers.onClientInit();
        ModBESR.onClientInit();
    }

    private static final class PlatformModelGeometryToSpecificationDelegator<T extends IModelSpecification<T>>
      implements IModelGeometry<PlatformModelGeometryToSpecificationDelegator<T>> {

        private final T delegate;

        private PlatformModelGeometryToSpecificationDelegator(final T delegate) {
            this.delegate = delegate;
        }

        @Override
        public BakedModel bake(
          final IModelConfiguration owner,
          final ModelBakery bakery,
          final Function<Material, TextureAtlasSprite> spriteGetter,
          final ModelState modelTransform,
          final ItemOverrides overrides,
          final ResourceLocation modelLocation)
        {
            return delegate.bake(
              bakery, spriteGetter, modelTransform, overrides, modelLocation
            );
        }

        @Override
        public Collection<Material> getTextures(
          final IModelConfiguration owner, final Function<ResourceLocation, UnbakedModel> modelGetter, final Set<Pair<String, String>> missingTextureErrors)
        {
            return delegate.getTextures(
              modelGetter, missingTextureErrors
            );
        }
    }

    private static final class PlatformModelLoaderDelegate<L extends IModelSpecificationLoader<T>, T extends IModelSpecification<T>>
      implements IModelLoader<PlatformModelGeometryToSpecificationDelegator<T>> {

        private final L delegate;

        private PlatformModelLoaderDelegate(final L delegate) {
            this.delegate = delegate;
        }

        @Override
        public @NotNull PlatformModelGeometryToSpecificationDelegator<T> read(final @NotNull JsonDeserializationContext deserializationContext, final @NotNull JsonObject modelContents)
        {
            return new PlatformModelGeometryToSpecificationDelegator<>(delegate.read(deserializationContext, modelContents));
        }

        @Override
        public void onResourceManagerReload(final @NotNull ResourceManager p_10758_)
        {
            delegate.onResourceManagerReload(p_10758_);
        }
    }
}
