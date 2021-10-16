package mod.chiselsandbits.api.plugin;

/**
 * Represents a plugin for ChiselsAndBits.
 *
 * Plugins have callbacks that can be invoked by aequivaleo.
 * See their documentation for more information.
 *
 * All methods are potentially invoked in parallel with other plugins, or even aequivaleo itself.
 */
public interface IChiselsAndBitsPlugin
{
    /**
     * The id of the plugin.
     * Has to be unique over all plugins.
     *
     * @return The id.
     */
    String getId();

    /**
     * Invoked when the plugin is constructed.
     */
    default void onConstruction() {};

    /**
     * Called after ChiselsAndBitss common setup completes.
     * Allows for the registration of static (none world specific) data.
     */
    default void onCommonSetup() {};
}
