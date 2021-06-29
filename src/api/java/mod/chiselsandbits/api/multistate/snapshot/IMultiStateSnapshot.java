package mod.chiselsandbits.api.multistate.snapshot;

import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.mutator.IAreaMutator;
import mod.chiselsandbits.api.multistate.statistics.IMultiStateObjectStatistics;
import net.minecraft.util.Direction;

public interface IMultiStateSnapshot extends IAreaMutator, Cloneable
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

    /**
     * Rotates the current multistate block 90 degrees around the given axis with the given rotation count.
     *
     * @param axis The axis to rotate around.
     * @param rotationCount The amount of times to rotate the
     */
    void rotate(final Direction.Axis axis, final int rotationCount);

    /**
     * Rotates the current multistate block exactly once 90 degrees around the given axis.
     *
     * @param axis The axis to rotate around.
     */
    default void rotate(final Direction.Axis axis) {
        this.rotate(axis, 1);
    }

    /**
     * Mirrors the current multistate block around the given axis.
     *
     * @param axis The axis to mirror over.
     */
    void mirror(final Direction.Axis axis);
}
