package mod.chiselsandbits.platforms.core.registries.deferred;

import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistry;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistryEntry;
import mod.chiselsandbits.platforms.core.registries.IPlatformRegistryManager;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Manages the deferred registration system for the underlying platform.
 */
public interface IRegistrarManager
{
    /**
     * Gives access to the deferred registrar manager.
     *
     * @return The deferred registrar manager.
     */
    static IRegistrarManager getInstance() {
        return IPlatformRegistryManager.getInstance().getDeferredRegistrarManager();
    }

    /**
     * Creates a new registry object for the given registry name.
     *
     * @param name The name of the object.
     * @param registryType A supplier that supplies the type of the registry to populate it in.
     * @param <U> The type of the object.
     * @param <T> The type of the registry, has to be a super type of U.
     * @return The registry object.
     */
    <U extends T, T extends IChiselsAndBitsRegistryEntry> IRegistryObject<U> newRegistryObject(ResourceLocation name, Supplier<Class<? super T>> registryType);

    /**
     * Creates a new registry object for the given registry name.
     *
     * @param name The name of the object.
     * @param registryType The registry to populate it in.
     * @param <U> The type of the object.
     * @param <T> The type of the registry, has to be a super type of U.
     * @return The registry object.
     */
    <U extends T, T extends IChiselsAndBitsRegistryEntry> IRegistryObject<U> newRegistryObject(ResourceLocation name, IChiselsAndBitsRegistry<T> registryType);

    /**
     * Creates a new registry object for the given registry name.
     *
     * @param name The name of the object to create.
     * @param baseType The base type of the object.
     * @param modId The mod id.
     * @param <U> The type of the object.
     * @param <T> The typed of the registry, has to be a super type of U.
     * @return The registry object.
     */
    <U extends T, T extends IChiselsAndBitsRegistryEntry> IRegistryObject<U> newRegistryObject(ResourceLocation name, Class<T> baseType, String modId);

    /**
     * Gets the empty registry object for the platform.
     *
     * @param <T> The type of the empty registry object.
     * @return The empty registry object.
     */
    <T extends IChiselsAndBitsRegistryEntry> IRegistryObject<T> emptyRegistryObject();

    /**
     * Returns a new registrar for the type given in the namespace of the mod id.
     *
     * @param typeClass The type of the registry for the registrar.
     * @param modId The mod if.
     * @param <T> The type in the registry.
     * @return The registrar for a registry of the given type in the given namespace.
     */
    <T extends IChiselsAndBitsRegistryEntry, R extends T> ICustomRegistrar<R> createCustomRegistrar(Class<T> typeClass, String modId);

    /**
     * Returns a new registrar for the type given in the namespace of the mod id.
     *
     * @param typeClass The type of the registry for the registrar.
     * @param modId The mod if.
     * @param <T> The type in the registry.
     * @return The registrar for a registry of the given type in the given namespace.
     */
    <T, R extends T> IRegistrar<R> createRegistrar(Class<T> typeClass, String modId);

    /**
     * Creates a new registry builder for the given registry type.
     *
     * @param <T> The type contained in the registry.
     * @return The registry builder.
     */
    <T extends IChiselsAndBitsRegistryEntry> IChiselsAndBitsRegistry.Builder<T> simpleBuilderFor();
}
