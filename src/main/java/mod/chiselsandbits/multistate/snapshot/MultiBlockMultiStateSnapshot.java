package mod.chiselsandbits.multistate.snapshot;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiBlockMultiStateSnapshot implements IMultiStateSnapshot
{
    private final Map<BlockPos, IMultiStateSnapshot> snapshots;
    private final Vector3d startPoint;
    private final Vector3d endPoint;

    public MultiBlockMultiStateSnapshot(final Map<BlockPos, IMultiStateSnapshot> snapshots, final Vector3d startPoint, final Vector3d endPoint) {
        this.snapshots = snapshots;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    /**
     * Creates a new area shape identifier.
     * <p>
     * Note: This method always returns a new instance.
     *
     * @return The new identifier.
     */
    @Override
    public IAreaShapeIdentifier createNewShapeIdentifier()
    {
        return null;
    }

    @Override
    public Stream<IStateEntryInfo> stream()
    {
        return snapshots.values()
          .stream()
          .flatMap(IAreaAccessor::stream);
    }

    /**
     * Gets the target state in the current area, using the offset from the area as well as the in area target offset.
     *
     * @param inAreaTarget The in area offset.
     * @return An optional potentially containing the state entry of the requested target.
     */
    @Override
    public Optional<IStateEntryInfo> getInAreaTarget(final Vector3d inAreaTarget)
    {
        return Optional.empty();
    }

    /**
     * Gets the target state in the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     * @return An optional potentially containing the state entry of the requested target.
     */
    @Override
    public Optional<IStateEntryInfo> getInBlockTarget(final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget)
    {
        return Optional.empty();
    }

    /**
     * Indicates if the given target is inside of the current accessor.
     *
     * @param inAreaTarget The area target to check.
     * @return True when inside, false when not.
     */
    @Override
    public boolean isInside(final Vector3d inAreaTarget)
    {
        return false;
    }

    /**
     * Indicates if the given target (with the given block position offset) is inside of the current accessor.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     * @return True when inside, false when not.
     */
    @Override
    public boolean isInside(final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget)
    {
        return false;
    }

    @Override
    public IMultiStateSnapshot createSnapshot()
    {
        final Map<BlockPos, IMultiStateSnapshot> copiedSnapshots = snapshots.keySet()
          .stream()
          .collect(Collectors.toMap(
            Function.identity(),
            pos -> snapshots.get(pos).createSnapshot()
          )
        );

        return new MultiBlockMultiStateSnapshot(
          copiedSnapshots,
          startPoint,
          endPoint
        );
    }

    /**
     * Returns all entries in the current area in a mutable fashion. Includes all empty areas as areas containing an air state.
     *
     * @return A stream with a mutable state entry info for each mutable section in the area.
     */
    @Override
    public Stream<IMutableStateEntryInfo> mutableStream()
    {
        return null;
    }

    @Override
    public void setInAreaTarget(final BlockState blockState, final Vector3d inAreaTarget) throws SpaceOccupiedException
    {
        final Vector3d workingTarget = inAreaTarget.add(this.startPoint);

        final BlockPos offset = new BlockPos(workingTarget);
        final Vector3d inBlockTarget = new Vector3d(
          workingTarget.getX() - offset.getX(),
          workingTarget.getY() - offset.getY(),
          workingTarget.getZ() - offset.getZ()
        );

        this.setInBlockTarget(
          blockState,
          offset,
          inBlockTarget
        );
    }

    @Override
    public void setInBlockTarget(final BlockState blockState, final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget) throws SpaceOccupiedException
    {
        final Vector3d workingTarget = Vector3d.copy(inAreaBlockPosOffset).add(inBlockTarget);
        if (workingTarget.getX() < startPoint.getX() ||
              workingTarget.getY() < startPoint.getY() ||
              workingTarget.getZ() < startPoint.getZ() ||
              workingTarget.getX() > endPoint.getX() ||
              workingTarget.getY() > endPoint.getY() ||
              workingTarget.getZ() > endPoint.getZ()
        )
            throw new IllegalArgumentException("The given target is outside of the operating range of this snapshot!");

        if (!snapshots.containsKey(inAreaBlockPosOffset))
            throw new IllegalArgumentException("The given in area block pos offset is outside of the target range!");

        this.snapshots.get(inAreaBlockPosOffset)
          .setInAreaTarget(blockState, inBlockTarget);
    }

    /**
     * Clears the current area, using the offset from the area as well as the in area target offset.
     *
     * @param inAreaTarget The in area offset.
     */
    @Override
    public void clearInAreaTarget(final Vector3d inAreaTarget)
    {

    }

    /**
     * Clears the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     */
    @Override
    public void clearInBlockTarget(final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget)
    {

    }

    /**
     * Converts the current snapshot to a variant which is itemstack capable.
     *
     * @return The multistate itemstack which is the itemstack nbt representation of the current snapshot.
     */
    @Override
    public IMultiStateItemStack toItemStack()
    {
        throw new NotImplementedException("Multi block snapshots can not be contained in an itemstack as of now.");
    }
}
