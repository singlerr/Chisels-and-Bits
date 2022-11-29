package mod.chiselsandbits.api.multistate.mutator;

import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;

public interface IMutableStateEntryInfo extends IStateEntryInfo
{
    /**
     * Sets the current entries block information.
     *
     * @param blockInformation The new block information of the entry.
     * @throws SpaceOccupiedException When the space is not clear and as such the bit can not be set.
     */
    void setBlockInformation(final IBlockInformation blockInformation) throws SpaceOccupiedException;

    /**
     * Clears the current state entries blockstate.
     * Effectively setting the current blockstate to air.
     */
    void clear();

    /**
     * Overrides the current entries block information
     *
     * @param blockInformation The new block information of the entry.
     */
    default void overrideState(final IBlockInformation blockInformation) {
        try
        {
            clear();
            setBlockInformation(blockInformation);
        }
        catch (SpaceOccupiedException ignored) //Should never be thrown, due to the clear call;
        {
        }
    }
}
