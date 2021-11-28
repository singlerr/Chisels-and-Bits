package mod.chiselsandbits.platforms.core;

import mod.chiselsandbits.platforms.core.blockstate.ILevelBasedPropertyAccessor;
import mod.chiselsandbits.platforms.core.chiseling.eligibility.IPlatformEligibilityOptions;
import mod.chiselsandbits.platforms.core.client.IClientManager;
import mod.chiselsandbits.platforms.core.config.IConfigurationManager;
import mod.chiselsandbits.platforms.core.dist.IDistributionManager;
import mod.chiselsandbits.platforms.core.entity.IEntityInformationManager;
import mod.chiselsandbits.platforms.core.entity.IPlayerInventoryManager;
import mod.chiselsandbits.platforms.core.event.IEventFirer;
import mod.chiselsandbits.platforms.core.fluid.IFluidManager;
import mod.chiselsandbits.platforms.core.inventory.bit.IAdaptingBitInventoryManager;
import mod.chiselsandbits.platforms.core.item.IDyeItemHelper;
import mod.chiselsandbits.platforms.core.item.IItemComparisonHelper;
import mod.chiselsandbits.platforms.core.network.INetworkChannelManager;
import mod.chiselsandbits.platforms.core.plugin.IPlatformPluginManager;
import mod.chiselsandbits.platforms.core.registries.IPlatformRegistryManager;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

/**
 * API Surface of a given game platform that is required to run Chisels and Bits.
 * Each game platform, like forge or fabric, has to provide this logic, outside
 * of Minecraft.
 *
 * Examples are registry access and systems to interact with the world.
 */
public interface IChiselsAndBitsPlatformCore
{
    /**
     * Gives access to the api instance.
     *
     * @return The api.
     */
    static IChiselsAndBitsPlatformCore getInstance()
    {
        return Holder.getInstance();
    }

    /**
     * Gives access to the current platform's registry.
     *
     * @return The registry manager for the current platform.
     */
    @NotNull
    IPlatformRegistryManager getPlatformRegistryManager();

    /**
     * Gives access to the current platform's way of processing entity information.
     *
     * @return The entity information manager.
     */
    @NotNull
    IEntityInformationManager getEntityInformationManager();

    /**
     * Gives access to the fluid manager of the current platform.
     *
     * @return The fluid manager of the current platform.
     */
    @NotNull
    IFluidManager getFluidManager();

    /**
     * The client manager for this platform.
     * Invoking this method on the server side, will throw an exception!
     *
     * @return The client manager.
     */
    @NotNull
    IClientManager getClientManager();

    /**
     * Gives access to level based property accessors.
     *
     * @return The accessor for level based properties.
     */
    @NotNull
    ILevelBasedPropertyAccessor getLevelBasedPropertyAccessor();

    /**
     * The item comparison helper of the platform at large.
     * Some platforms extend the functionality of itemstacks beyond item, meta and nbt.
     * And sometimes these values have to be taken into account while comparing itemstacks.
     *
     * @return The item comparison helper.
     */
    @NotNull
    IItemComparisonHelper getItemComparisonHelper();

    /**
     * The eligibility manager that is active for the current platform.
     * Allows for the modification of the eligibility analysis on the given platform.
     * Useful in case the platform defines different default classes for the processing logic.
     *
     * @return The platform's eligibility manager.
     */
    @NotNull
    IPlatformEligibilityOptions getPlatformEligibilityOptions();

    /**
     * Gives access to a system which can fire event data on the given platform.
     *
     * @return The system to fire the events.
     */
    @NotNull
    IEventFirer getEventFirer();

    /**
     * Gives access to the player inventory manager.
     *
     * @return The player inventory manager.
     */
    @NotNull
    IPlayerInventoryManager getPlayerInventoryManager();

    /**
     * Gives access to the distribution manager.
     *
     * @return The distribution manager.
     */
    @NotNull
    IDistributionManager getDistributionManager();

    /**
     * Gives access to the network manager.
     *
     * @return The network manager.
     */
    @NotNull
    INetworkChannelManager getNetworkChannelManager();

    /**
     * Gives access to the platform's plugin manager.
     *
     * @return The platform's plugin manager.
     */
    @NotNull
    IPlatformPluginManager getPlatformPluginManager();

    /**
     * Gives access to the dye item helper on the platform.
     *
     * @return The dye item helper.
     */
    @NotNull
    IDyeItemHelper getDyeItemHelper();

    /**
     * The configuration manager for the current platform.
     *
     * @return The configuration manager.
     */
    @NotNull
    IConfigurationManager getConfigurationManager();

    /**
     * The adapting bit inventory manager.
     * Allows for the platform to adapt specific inputs to bit inventories.
     *
     * @return The bit inventory manager for adapting platform specific inventories to bit inventories.
     */
    @NotNull
    IAdaptingBitInventoryManager getAdaptingBitInventoryManager();

    /**
     * Gives access to the current server platform.
     *
     * @return The current server running.
     */
    @NotNull
    MinecraftServer getCurrentServer();

    class Holder {
        private static IChiselsAndBitsPlatformCore apiInstance;

        public static IChiselsAndBitsPlatformCore getInstance()
        {
            return apiInstance;
        }

        public static void setInstance(final IChiselsAndBitsPlatformCore instance)
        {
            if (apiInstance != null)
                throw new IllegalStateException("Can not setup API twice!");

            apiInstance = instance;
        }
    }
}
