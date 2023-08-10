package mod.chiselsandbits.api.item.multistate;

import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.util.INBTSerializable;
import net.minecraft.nbt.CompoundTag;

import java.util.Set;

/**
 * The statistics of a multi state itemstack.
 */
public interface IStatistics extends INBTSerializable<CompoundTag>
{

    /**
     * The primary state of the mutli state itemstacks this statistics object
     * belongs to.
     *
     * @return The primary blockstate.
     */
    IBlockInformation getPrimaryState();

    /**
     * Indicates if the multistate object is empty.
     *
     * @return {@code true} for an empty multi state object.
     */
    boolean isEmpty();

    /**
     * Returns all states (without count) that are contained in the object
     *
     * @return All states in the object.
     */
    Set<IBlockInformation> getContainedStates();
}
