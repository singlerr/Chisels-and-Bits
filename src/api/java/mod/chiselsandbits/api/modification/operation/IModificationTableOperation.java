package mod.chiselsandbits.api.modification.operation;

import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * A modification operation that can be performed in the modification table.
 */
public interface IModificationTableOperation extends IForgeRegistryEntry<IModificationTableOperation>
{

    /**
     * Performs a modification on the snapshot.
     *
     * @param source The source
     * @return The modified snapshot.
     */
    IMultiStateSnapshot apply(final IMultiStateSnapshot source);
}
