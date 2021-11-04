package mod.chiselsandbits.platforms.core.client.models.data;

import mod.chiselsandbits.platforms.core.client.IClientManager;

/**
 * Represents a key in model data of a specific type.
 */
public interface IModelDataKey<T>
{

    /**
     * Creates a new model data key for the given type.
     * Each returned instance is unique.
     *
     * @param <T> The type of the key.
     * @return The unique model data key.
     */
    static <T> IModelDataKey<T> create() {
        return IClientManager.getInstance().createNewModelDataKey();
    }
}
