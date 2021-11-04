package mod.chiselsandbits.platforms.core.client;

import mod.chiselsandbits.platforms.core.IChiselsAndBitsPlatformCore;
import mod.chiselsandbits.platforms.core.client.models.data.IModelDataBuilder;
import mod.chiselsandbits.platforms.core.client.models.data.IModelDataKey;
import mod.chiselsandbits.platforms.core.client.rendering.IRenderingManager;
import org.jetbrains.annotations.NotNull;

/**
 * The client manager gives access to client related features of the platform.
 */
public interface IClientManager
{
    /**
     * The client manager for this platform.
     * Invoking this method on the server side, will throw an exception!
     *
     * @return The client manager.
     */
    static IClientManager getInstance() {
        return IChiselsAndBitsPlatformCore.getInstance().getClientManager();
    }

    /**
     * Gives access to the clients rendering manager.
     *
     * @return The client rendering manager.
     */
    @NotNull
    IRenderingManager getRenderingManager();

    /**
     * Creates a new model data builder instance.
     *
     * @return The model data instance.
     */
    @NotNull
    IModelDataBuilder createNewModelDataBuilder();

    /**
     * Creates a new model data key for the given type.
     * Each returned instance is unique.
     *
     * @param <T> The type of the key.
     * @return The unique model data key.
     */
    @NotNull
    <T> IModelDataKey<T> createNewModelDataKey();
}
