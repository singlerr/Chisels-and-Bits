package mod.chiselsandbits.api.item.multistate;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.item.ItemStack;

/**
 * Can create multistate items from a given source.
 */
public interface IMultiStateItemFactory
{

    static IMultiStateItemFactory getInstance() {
        return IChiselsAndBitsAPI.getInstance().getMultiStateItemFactory();
    }

    /**
     * Creates a new multistate itemstack with a single state internally.
     * also be using the same logic.
     *
     * @param stateEntryInfo The state entry info to create an itemstack for.
     * @return The itemstack containing only the given single state entry.
     */
    ItemStack createFrom(final IStateEntryInfo stateEntryInfo);
}
