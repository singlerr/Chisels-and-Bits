package mod.chiselsandbits.api.plugin;

import com.google.common.collect.ImmutableSet;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;

/**
 * The manager for plugins.
 */
public interface IChiselsAndBitsPluginManager
{

    /**
     * The instance of the plugin manager.
     *
     * @return The plugin manager.
     */
    static IChiselsAndBitsPluginManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getPluginManager();
    }

    /**
     * Gets the plugins.
     *
     * @return An immutable set with the plugins.
     */
    ImmutableSet<IChiselsAndBitsPlugin> getPlugins();
}
