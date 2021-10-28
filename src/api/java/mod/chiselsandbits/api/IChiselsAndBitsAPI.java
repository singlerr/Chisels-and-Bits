package mod.chiselsandbits.api;

import mod.chiselsandbits.api.addons.IChiselsAndBitsAddon;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.chiseling.IChiselingManager;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.chiseling.ILocalChiselingContextCache;
import mod.chiselsandbits.api.client.chiseling.preview.render.IChiselContextPreviewRendererRegistry;
import mod.chiselsandbits.api.client.tool.mode.icon.ISelectedToolModeIconRendererRegistry;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemFactory;
import mod.chiselsandbits.api.measuring.IMeasuringManager;
import mod.chiselsandbits.api.modification.operation.IModificationOperation;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhoodBuilder;
import mod.chiselsandbits.api.plugin.IChiselsAndBitsPluginManager;
import mod.chiselsandbits.api.profiling.IProfilingManager;
import mod.chiselsandbits.api.registries.IRegistryManager;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Do not implement, is passed to your {@link IChiselsAndBitsAddon},
 * and can be accessed via its {@link #getInstance()}-method.
 */
public interface IChiselsAndBitsAPI
{
    /**
     * Gives access to the api instance.
     *
     * @return The api.
     */
    static IChiselsAndBitsAPI getInstance()
    {
        return Holder.getInstance();
    }

    /**
     * Gives access to the factory that can produce different mutators.
     *
     * @return The factory used to create new mutators.
     */
    @NotNull
    IMutatorFactory getMutatorFactory();

    /**
     * Manager which deals with chiseling eligibility.
     *
     * @return The manager.
     */
    @NotNull
    IEligibilityManager getEligibilityManager();

    /**
     * Manager which deals with converting eligible blocks, blockstates and IItemProviders into their chiseled
     * variants.
     *
     * @return The conversion manager.
     */
    @NotNull
    IConversionManager getConversionManager();

    /**
     * Manager which deals with calculating, and optionally caching, the voxel shapes, which
     * can be constructed from a given area.
     *
     * @return The voxel shape manager.
     */
    @NotNull
    IVoxelShapeManager getVoxelShapeManager();

    /**
     * A factory which can produce a multistate item from a given source.
     *
     * @return The factory.
     */
    @NotNull
    IMultiStateItemFactory getMultiStateItemFactory();

    /**
     * Represents the default mode for the chiseling system.
     *
     * @return The default mode.
     */
    @NotNull
    IChiselMode getDefaultChiselMode();

    /**
     * Gives access to all registries which are used by chisels and bits.
     *
     * @return The manager for registries used by chisels and bits.
     */
    @NotNull
    IRegistryManager getRegistryManager();

    /**
     * Gives access to the manager which controls chiseling operations.
     *
     * @return The current chiseling manager.
     */
    @NotNull
    IChiselingManager getChiselingManager();

    /**
     * The configuration on top of which chisels and bits is running.
     *
     * @return The current configuration.
     */
    @NotNull
    Configuration getConfiguration();

    /**
     * The manager which deals with calculating the given blockstate ids in the current running session.
     *
     * @return The blockstate id manager.
     */
    @NotNull
    IBlockStateIdManager getBlockStateIdManager();

    /**
     * Gives access to the bits inventory manager, which allows the conversion of normal inventory systems to bit inventories.
     * These special bit inventories respect the core interfaces that make up an object that can hold or is a bit.
     *
     * @return The manager for dealing with bits.
     */
    @NotNull
    IBitInventoryManager getBitInventoryManager();

    /**
     * The bit item manager.
     * Allows for the creation of bit based itemstacks.
     *
     * @return The bit item manager.
     */
    @NotNull
    IBitItemManager getBitItemManager();

    /**
     * The measuring manager.
     * Gives access to measurements created by a given player.
     *
     * @return The measuring manager.
     */
    @NotNull
    IMeasuringManager getMeasuringManager();

    /**
     * Represents the size of the bits in the current instance.
     *
     * @return The size of the state entries in the current instance.
     */
    @NotNull
    default StateEntrySize getStateEntrySize() {
        return getConfiguration().getServer().bitSize.get();
    }

    /**
     * The profiling manager, allows for the profiling of operations related Chisels and Bits.
     *
     * @return The profiling manager.
     */
    @NotNull
    IProfilingManager getProfilingManager();

    /**
     * This method gives access to the client side local chiseling context cache.
     * Although this method also exists on the server side, it should be considered a cross tick cache for the latest chiseling context in use by the current player,
     * without it becoming the active context for that player.
     *
     * @return The {@link ILocalChiselingContextCache}.
     */
    @NotNull
    ILocalChiselingContextCache getLocalChiselingContextCache();

    /**
     * The change tracker manager.
     * Gives access to each players change tracker.
     *
     * @return The change tracker manager
     */
    @NotNull
    IChangeTrackerManager getChangeTrackerManager();

    /**
     * Gives access to the block neighborhood builder.
     * Allows for building block specific cache keys when the block environment is required.
     *
     * @return The block neighborhood builder.
     */
    @NotNull
    IBlockNeighborhoodBuilder getBlockNeighborhoodBuilder();

    /**
     * The default mode for performing modification operations if no other is supplied.
     * @return The default modification operation.
     */
    @NotNull
    IModificationOperation getDefaultModificationOperation();

    /**
     * Gives access to the plugin manager that is used to process chisels and bits plugins
     * @return The plugin manager
     */
    @NotNull
    IChiselsAndBitsPluginManager getPluginManager();

    /**
     * Gives access to the chisel context preview renderer registry.
     * @return The registry.
     */
    @OnlyIn(Dist.CLIENT)
    @NotNull
    IChiselContextPreviewRendererRegistry getChiselContextPreviewRendererRegistry();

    /**
     * Gives access to the selected tool mode icon renderer registry.
     * @return The registry.
     */
    @OnlyIn(Dist.CLIENT)
    @NotNull
    ISelectedToolModeIconRendererRegistry getSelectedToolModeIconRenderer();

    class Holder {
        private static IChiselsAndBitsAPI apiInstance;

        public static IChiselsAndBitsAPI getInstance()
        {
            return apiInstance;
        }

        public static void setInstance(final IChiselsAndBitsAPI instance)
        {
            if (apiInstance != null)
                throw new IllegalStateException("Can not setup API twice!");

            apiInstance = instance;
        }
    }
}
