package mod.chiselsandbits.api.modification.operation;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.item.withmode.IToolMode;
import mod.chiselsandbits.api.multistate.mutator.IGenerallyModifiableAreaMutator;
import mod.chiselsandbits.api.registries.IRegistryManager;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistry;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistryEntry;

/**
 * A modification operation that can be performed in the modification table.
 */
public interface IModificationOperation extends IChiselsAndBitsRegistryEntry, IToolMode<IModificationOperationGroup>
{

    /**
     * The default modification operation.
     * @return The default operation.
     */
    static IModificationOperation getDefaultMode() {
        return IChiselsAndBitsAPI.getInstance().getDefaultModificationOperation();
    }

    /**
     * The underlying registry that contains the different modification modes that can be performed.
     * @return The underlying forge registry.
     */
    static IChiselsAndBitsRegistry<IModificationOperation> getRegistry() {
        return IRegistryManager.getInstance().getModificationOperationRegistry();
    }

    /**
     * Performs a modification on the snapshot.
     *
     * @param source The mutator to modify.
     */
    void apply(final IGenerallyModifiableAreaMutator source);
}
