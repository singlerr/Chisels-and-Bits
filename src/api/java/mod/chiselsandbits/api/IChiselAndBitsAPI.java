package mod.chiselsandbits.api;

import mod.chiselsandbits.api.addons.IChiselsAndBitsAddon;
import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.chiseling.IConversionManager;
import mod.chiselsandbits.api.chiseling.IEligibilityManager;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Do not implement, is passed to your {@link IChiselsAndBitsAddon},
 * and can be accessed via its {@link #getInstance()}-method.
 */
public interface IChiselAndBitsAPI
{
    /**
     * Gives access to the api instance.
     *
     * @return The api.
     */
    static IChiselAndBitsAPI getInstance()
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

    class Holder {
        private static IChiselAndBitsAPI apiInstance;

        public static IChiselAndBitsAPI getInstance()
        {
            return apiInstance;
        }

        public static void setInstance(final IChiselAndBitsAPI instance)
        {
            if (apiInstance != null)
                throw new IllegalStateException("Can not setup API twice!");

            apiInstance = instance;
        }
    }
}
