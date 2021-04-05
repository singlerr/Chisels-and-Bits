package mod.chiselsandbits.api.block.entity;

import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.multistate.statistics.IMultiStateObjectStatistics;
import mod.chiselsandbits.api.multistate.accessor.world.IWorldAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Represents the block entity with the state data, which under-ly the information
 * provided by the {@link IMultiStateBlock} blocks.
 */
public interface IMultiStateBlockEntity extends IWorldAreaAccessor,
                                                          IWorldAreaMutator,
                                                          INBTSerializable<CompoundNBT>,
                                                          IPacketBufferSerializable
{

    /**
     * The statistics of this block.
     *
     * @return The statistics.
     */
    IMultiStateObjectStatistics getStatistics();

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
     * Initializes the block entity so that all its state entries
     * have the given state as their state.
     *
     * @param currentState The new initial state.
     */
    void initializeWith(BlockState currentState);
}
