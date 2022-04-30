package mod.chiselsandbits.api.glueing.operation;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.item.withmode.IToolMode;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.IGenerallyModifiableAreaMutator;
import mod.chiselsandbits.api.registries.IRegistryManager;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistry;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistryEntry;

import java.util.Collection;

/**
 * A Glueing operation that can be performed in the Glueing table.
 */
public interface IGlueingOperation extends IChiselsAndBitsRegistryEntry, IToolMode<IGlueingOperationGroup>
{
    /**
     * The default Glueing operation.
     * @return The default operation.
     */
    static IGlueingOperation getDefaultMode() {
        return IChiselsAndBitsAPI.getInstance().getDefaultGlueingOperation();
    }

    /**
     * The underlying registry that contains the different Glueing modes that can be performed.
     * @return The underlying forge registry.
     */
    static IChiselsAndBitsRegistry<IGlueingOperation> getRegistry() {
        return IRegistryManager.getInstance().getGlueingOperationRegistry();
    }

    /**
     * Performs a glueing on the given snapshots.
     * The given collection contains the individual pieces that are needed to be glued together.
     *
     * In total a maximum 64 pieces can be given.
     *
     * @param sources The accessors to glue.
     * @return An accessor which was created by the operation.
     */
    IAreaAccessor apply(final Collection<IAreaAccessor> sources);
}
