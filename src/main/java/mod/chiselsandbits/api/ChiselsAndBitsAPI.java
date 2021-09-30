package mod.chiselsandbits.api;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.chiseling.IChiselingManager;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.chiseling.ILocalChiselingContextCache;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemFactory;
import mod.chiselsandbits.api.measuring.IMeasuringManager;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhoodBuilder;
import mod.chiselsandbits.api.profiling.IProfilingManager;
import mod.chiselsandbits.api.registries.IRegistryManager;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
import mod.chiselsandbits.change.ChangeTracker;
import mod.chiselsandbits.change.ChangeTrackerManger;
import mod.chiselsandbits.chiseling.conversion.ConversionManager;
import mod.chiselsandbits.chiseling.eligibility.EligibilityManager;
import mod.chiselsandbits.chiseling.LocalChiselingContextCache;
import mod.chiselsandbits.inventory.management.BitInventoryManager;
import mod.chiselsandbits.item.bit.BitItemManager;
import mod.chiselsandbits.item.multistate.MultiStateItemFactory;
import mod.chiselsandbits.measures.MeasuringManager;
import mod.chiselsandbits.multistate.mutator.MutatorFactory;
import mod.chiselsandbits.neighborhood.BlockNeighborhoodBuilder;
import mod.chiselsandbits.profiling.ProfilingManager;
import mod.chiselsandbits.registrars.ModChiselModes;
import mod.chiselsandbits.registries.RegistryManager;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import org.jetbrains.annotations.NotNull;

public class ChiselsAndBitsAPI implements IChiselsAndBitsAPI
{
    private static final ChiselsAndBitsAPI INSTANCE = new ChiselsAndBitsAPI();

    private ChiselsAndBitsAPI()
    {
    }

    public static ChiselsAndBitsAPI getInstance()
    {
        return INSTANCE;
    }

    /**
     * Gives access to the factory that can produce different mutators.
     *
     * @return The factory used to create new mutators.
     */
    @NotNull
    @Override
    public IMutatorFactory getMutatorFactory()
    {
        return MutatorFactory.getInstance();
    }

    /**
     * Manager which deals with chiseling eligibility.
     *
     * @return The manager.
     */
    @NotNull
    @Override
    public IEligibilityManager getEligibilityManager()
    {
        return EligibilityManager.getInstance();
    }

    /**
     * Manager which deals with converting eligible blocks, blockstates and IItemProviders into their chiseled variants.
     *
     * @return The conversion manager.
     */
    @NotNull
    @Override
    public IConversionManager getConversionManager()
    {
        return ConversionManager.getInstance();
    }

    /**
     * Manager which deals with calculating, and optionally caching, the voxel shapes, which can be constructed from a given area.
     *
     * @return The voxel shape manager.
     */
    @NotNull
    @Override
    public IVoxelShapeManager getVoxelShapeManager()
    {
        return VoxelShapeManager.getInstance();
    }

    /**
     * A factory which can produce a multistate item from a given source.
     *
     * @return The factory.
     */
    @NotNull
    @Override
    public IMultiStateItemFactory getMultiStateItemFactory()
    {
        return MultiStateItemFactory.getInstance();
    }

    /**
     * Represents the default mode for the chiseling system.
     *
     * @return The default mode.
     */
    @NotNull
    @Override
    public IChiselMode getDefaultChiselMode()
    {
        return ModChiselModes.SINGLE_BIT.get();
    }

    /**
     * Gives access to all registries which are used by chisels and bits.
     *
     * @return The manager for registries used by chisels and bits.
     */
    @NotNull
    @Override
    public IRegistryManager getRegistryManager()
    {
        return RegistryManager.getInstance();
    }

    /**
     * Gives access to the manager which controls chiseling operations.
     *
     * @return The current chiseling manager.
     */
    @NotNull
    @Override
    public IChiselingManager getChiselingManager()
    {
        return mod.chiselsandbits.chiseling.ChiselingManager.getInstance();
    }

    /**
     * The configuration on top of which chisels and bits is running.
     *
     * @return The current configuration.
     */
    @NotNull
    @Override
    public Configuration getConfiguration()
    {
        return ChiselsAndBits.getInstance().getConfiguration();
    }

    /**
     * The manager which deals with calculating the given blockstate ids in the current running session.
     *
     * @return The blockstate id manager.
     */
    @NotNull
    @Override
    public IBlockStateIdManager getBlockStateIdManager()
    {
        return new IBlockStateIdManager() {};
    }

    @NotNull
    @Override
    public IBitInventoryManager getBitInventoryManager()
    {
        return BitInventoryManager.getInstance();
    }

    @NotNull
    @Override
    public IBitItemManager getBitItemManager()
    {
        return BitItemManager.getInstance();
    }

    @Override
    public @NotNull IMeasuringManager getMeasuringManager()
    {
        return MeasuringManager.getInstance();
    }

    @Override
    public @NotNull IProfilingManager getProfilingManager()
    {
        return ProfilingManager.getInstance();
    }

    @Override
    public @NotNull ILocalChiselingContextCache getLocalChiselingContextCache()
    {
        return LocalChiselingContextCache.getInstance();
    }

    @Override
    public @NotNull IChangeTrackerManager getChangeTrackerManager()
    {
        return ChangeTrackerManger.getInstance();
    }

    @Override
    public @NotNull IBlockNeighborhoodBuilder getBlockNeighborhoodBuilder()
    {
        return BlockNeighborhoodBuilder.getInstance();
    }
}
