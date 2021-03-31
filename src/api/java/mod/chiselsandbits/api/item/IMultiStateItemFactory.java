package mod.chiselsandbits.api.item;

import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.item.ItemStack;

/**
 * Can create multistate items from a given source.
 */
public interface IMultiStateItemFactory
{

    static IMultiStateItemFactory getInstance() {
        return IChiselAndBitsAPI.getInstance().getMultiStateItemFactory();
    }

    /**
     * Creates a new multistate itemstack from a given snapshot.
     *
     * @param snapshot The snapshot in question.
     * @return The multistate item.
     */
    ItemStack createFrom(final IMultiStateSnapshot snapshot);

    /**
     * Creates a new multistate itemstack with a single state internally.
     * The returned stack might contain an item which is fundamentally different
     * then the one returned from {@link #createFrom(IMultiStateSnapshot)}, however this might
     * also be using the same logic.
     *
     * @param stateEntryInfo The state entry info to create an itemstack for.
     * @return The itemstack containing only the given single state entry.
     */
    ItemStack createFrom(final IStateEntryInfo stateEntryInfo);
}
