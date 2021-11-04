package mod.chiselsandbits.platforms.core.registries;

import mod.chiselsandbits.platforms.core.registries.deferred.IRegistrarManager;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a registry that can be used to register expandable systems to.
 * @param <T> The type of the element in the registry.
 */
public interface IChiselsAndBitsRegistry<T extends IChiselsAndBitsRegistryEntry>
{

    /**
     * Gives access to all values in the registry.
     * @return The registries values.
     */
    Collection<T> getValues();

    /**
     * Gives access to all names stored in the current registry.
     * @return All names as "ids" of the objects stored in the registry.
     */
    Set<ResourceLocation> getNames();

    /**
     * Gives access to the value with the given name in the registry.
     * An empty optional is returned if no object is registered with the given name.
     *
     * @param name The name to lookup.
     * @return An optional with the lookup result, empty if the name is not used by any object in the registry.
     */
    Optional<T> get(final ResourceLocation name);

    /**
     * Builder specifications for creating new chisels and bits registries.
     *
     * @param <T> The type contained in the registry.
     */
    interface Builder<T extends IChiselsAndBitsRegistryEntry> {

        /**
         * Creates a new registry builder for the given registry type.
         *
         * @param <T> The type contained in the registry.
         * @return The registry builder.
         */
        static <T extends IChiselsAndBitsRegistryEntry> IChiselsAndBitsRegistry.Builder<T> simple()
        {
            return IRegistrarManager.getInstance().simpleBuilderFor();
        }

        /**
         * Creates a new registry from the current specification.
         *
         * @return The new registry.
         */
        IChiselsAndBitsRegistry<T> build();
    }
}
