package mod.chiselsandbits.api.multistate.snapshot;

import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.mutator.IGenerallyModifiableAreaMutator;
import mod.chiselsandbits.api.multistate.statistics.IMultiStateObjectStatistics;

public interface IMultiStateSnapshot extends Cloneable, IGenerallyModifiableAreaMutator
{

    /**
     * Converts the current snapshot to a variant which is itemstack capable.
     *
     * @return The multistate itemstack which is the itemstack nbt representation of the current snapshot.
     */
    IMultiStateItemStack toItemStack();

    /**
     * Returns the statistics of the current snapshot.
     *
     * @return The statistics
     */
    IMultiStateObjectStatistics getStatics();

    /**
     * Creates a clone of the snapshot.
     *
     * @return The clone.
     */
    IMultiStateSnapshot clone();


}
