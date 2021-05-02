package mod.chiselsandbits.api.multistate.accessor;

import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.stream.Stream;

/**
 * Gives access to all states in a given area.
 * Might be larger then a single block.
 */
public interface IAreaAccessor extends IStateAccessor
{
    /**
     * Creates a new area shape identifier.
     *
     * Note: This method always returns a new instance.
     *
     * @return The new identifier.
     */
    IAreaShapeIdentifier createNewShapeIdentifier();

    /**
     * Gives access to a stream with the entry state info inside the accessors range.
     *
     * @return The stream with the inner states.
     */
    Stream<IStateEntryInfo> stream();

    /**
     * Indicates if the given target is inside of the current accessor.
     *
     * @param inAreaTarget The area target to check.
     * @return True when inside, false when not.
     */
    boolean isInside(final Vector3d inAreaTarget);

    /**
     * Indicates if the given target (with the given block position offset) is inside of the current accessor.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     * @return True when inside, false when not.
     */
    boolean isInside(
      BlockPos inAreaBlockPosOffset,
      Vector3d inBlockTarget
    );

    /**
     * Creates a snapshot of the current state.
     *
     * @return The snapshot.
     */
    IMultiStateSnapshot createSnapshot();

    /**
     * Gives access to a stream with the entry state info inside the accessors range.
     * Allows for the entry state order to be mutated using a position mutator.
     *
     * @param positionMutator The mutator for the positional order.
     * @return The stream with the inner states.
     */
    Stream<IStateEntryInfo> streamWithPositionMutator(IPositionMutator positionMutator);
}
