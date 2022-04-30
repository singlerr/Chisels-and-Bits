package mod.chiselsandbits.forge.platform.registry.registrar;

import mod.chiselsandbits.platforms.core.registries.deferred.impl.custom.CustomRegistryManager;
import mod.chiselsandbits.forge.platform.registry.registrar.delegates.ForgeDeferredRegisterPlatformDelegate;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistry;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistryEntry;
import mod.chiselsandbits.platforms.core.registries.deferred.ICustomRegistrar;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistrar;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistrarManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ForgeRegistrarManager implements IRegistrarManager
{
    private static final ForgeRegistrarManager INSTANCE = new ForgeRegistrarManager();

    public static ForgeRegistrarManager getInstance()
    {
        return INSTANCE;
    }

    private ForgeRegistrarManager()
    {
    }

    @Override
    public <T extends IChiselsAndBitsRegistryEntry, R extends T> ICustomRegistrar<R> createCustomRegistrar(
      final Class<T> typeClass, final String modId)
    {
        return CustomRegistryManager.getInstance().createNewRegistrar(typeClass, modId);
    }

    @Override
    public <T, R extends T> IRegistrar<R> createRegistrar(final ResourceKey<? extends Registry<T>> key, final String modId)
    {
        final DeferredRegister register = DeferredRegister.create((ResourceKey) key, modId);

        register.register(FMLJavaModLoadingContext.get().getModEventBus());

        return new ForgeDeferredRegisterPlatformDelegate(
          register
        );
    }

    @Override
    public <T extends IChiselsAndBitsRegistryEntry> IChiselsAndBitsRegistry.Builder<T> simpleBuilderFor()
    {
        return CustomRegistryManager.getInstance().createNewSimpleBuilder();
    }
}
