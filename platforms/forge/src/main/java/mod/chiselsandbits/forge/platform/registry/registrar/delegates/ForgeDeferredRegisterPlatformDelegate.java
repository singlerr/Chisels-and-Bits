package mod.chiselsandbits.forge.platform.registry.registrar.delegates;

import mod.chiselsandbits.platforms.core.registries.deferred.IRegistrar;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.function.Supplier;

public class ForgeDeferredRegisterPlatformDelegate<T extends IForgeRegistryEntry<T>> implements IRegistrar<T>
{

    private final DeferredRegister<T> delegate;

    public ForgeDeferredRegisterPlatformDelegate(final DeferredRegister<T> delegate) {this.delegate = delegate;}

    @Override
    public <I extends T> IRegistryObject<I> register(final String name, final Supplier<? extends I> factory)
    {
        return new ForgeRegistryObjectPlatformDelegate<>(
          delegate.register(name, factory)
        );
    }
}
