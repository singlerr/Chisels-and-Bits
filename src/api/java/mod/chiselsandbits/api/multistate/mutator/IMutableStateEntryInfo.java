package mod.chiselsandbits.api.multistate.mutator;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.world.IInWorldStateEntryInfo;
import net.minecraft.block.BlockState;

public interface IMutableStateEntryInfo extends IInWorldStateEntryInfo
{
    /**
     * Sets the current entries state.
     *
     * @param blockState The new blockstate of the entry.
     */
    void setState(final BlockState blockState) throws SpaceOccupiedException;

    /**
     * Clears the current state entries blockstate.
     * Effectively setting the current blockstate to air.
     */
    void clear();

    /**
     * Overrides the current entries state.
     *
     * @param blockState The new blockstate of the entry.
     */
    default void overrideState(final BlockState blockState) {
        clear();
        try
        {
            setState(blockState);
        }
        catch (SpaceOccupiedException ignored) //Should never be thrown, due to the clear call;
        {
        }
    }
}
