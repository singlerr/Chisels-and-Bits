package mod.chiselsandbits.api;

import mod.chiselsandbits.api.addons.IChiselsAndBitsAddon;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.chiseling.IChiselingManager;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemFactory;
import mod.chiselsandbits.api.measuring.IMeasuringManager;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.registries.IRegistryManager;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
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
     * The chiseling change tracker.
     *
     * @return The change tracker.
     */
    @NotNull
    IChangeTracker getChangeTracker();

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

    @NotNull
    IBitInventoryManager getBitInventoryManager();

    @NotNull
    IBitItemManager getBitItemManager();

    @NotNull
    IMeasuringManager getMeasuringManager();

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
