package mod.chiselsandbits.api.multistate.snapshot;

import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.mutator.IAreaMutator;

public interface IMultiStateSnapshot extends IAreaMutator
{

    /**
     * Converts the current snapshot to a variant which is itemstack capable.
     *
     * @return The multistate itemstack which is the itemstack nbt representation of the current snapshot.
     */
    IMultiStateItemStack toItemStack();
}
