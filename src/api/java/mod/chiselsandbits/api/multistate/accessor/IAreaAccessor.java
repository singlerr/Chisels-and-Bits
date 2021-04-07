package mod.chiselsandbits.api.multistate.accessor;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Gives access to all states in a given area.
 * Might be larger then a single block.
 */
public interface IAreaAccessor
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
     * Gets the target state in the current area, using the offset from the area as well as the in area target offset.
     *
     * @param inAreaTarget The in area offset.
     * @return An optional potentially containing the state entry of the requested target.
     */
    Optional<IStateEntryInfo> getInAreaTarget(
      Vector3d inAreaTarget
    );

    /**
     * Gets the target state in the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     * @return An optional potentially containing the state entry of the requested target.
     */
    Optional<IStateEntryInfo> getInBlockTarget(
      BlockPos inAreaBlockPosOffset,
      Vector3d inBlockTarget
    );

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
}
