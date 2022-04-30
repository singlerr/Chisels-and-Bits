package mod.chiselsandbits.platforms.core.registries.deferred.impl.custom;

import com.google.common.collect.Maps;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistry;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistryEntry;
import mod.chiselsandbits.platforms.core.registries.deferred.ICustomRegistrar;

import java.util.Map;

@SuppressWarnings("unchecked")
public class CustomRegistryManager
{
    private static final CustomRegistryManager INSTANCE = new CustomRegistryManager();

    public static CustomRegistryManager getInstance()
    {
        return INSTANCE;
    }

    private final Map<Class<?>, Map<String, CustomRegistrar<?>>> registrarMap = Maps.newConcurrentMap();

    private CustomRegistryManager()
    {
    }

    public <R extends T, T extends IChiselsAndBitsRegistryEntry> ICustomRegistrar<R> createNewRegistrar(final Class<T> typeClass, final String modId)
    {
        if (registrarMap.containsKey(typeClass) && registrarMap.get(typeClass).containsKey(modId))
            return (ICustomRegistrar<R>) registrarMap.get(typeClass).get(modId);

        final CustomRegistrar<R> registrar = new CustomRegistrar<>(modId);
        registrarMap.computeIfAbsent(typeClass, k -> Maps.newConcurrentMap()).put(modId, registrar);

        return registrar;
    }

    public <T extends IChiselsAndBitsRegistryEntry> IChiselsAndBitsRegistry.Builder<T> createNewSimpleBuilder()
    {
        return new CustomRegistry.Builder<>();
    }
}
