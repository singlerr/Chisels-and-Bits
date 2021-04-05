package mod.chiselsandbits.multistate.mutator;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.multistate.snapshot.MultiBlockMultiStateSnapshot;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldWrappingMutator implements IWorldAreaMutator
{

    private final IWorld world;
    private final Vector3d     startPoint;
    private final Vector3d endPoint;

    public WorldWrappingMutator(final IWorld world, final Vector3d startPoint, final Vector3d endPoint) {
        this.world = world;
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
        final BlockPos startBlockPos = new BlockPos(getStartPoint());
        final BlockPos endBlockPos = new BlockPos(getEndPoint());

        Stream<BlockPos> positionStream = startBlockPos.equals(endBlockPos) ? Stream.of(startBlockPos) : BlockPosStreamProvider.getForRange(
          startBlockPos.getX(), startBlockPos.getY(), startBlockPos.getZ(),
          endBlockPos.getX(), endBlockPos.getY(), endBlockPos.getZ()
        );

        return positionStream.map(blockPos -> new ChiselAdaptingWorldMutator(getWorld(), blockPos))
          .flatMap(ChiselAdaptingWorldMutator::inWorldStream)
          .filter(entry -> !(entry.getInWorldStartPoint().getX() < getStartPoint().getX()) &&
                   !(entry.getInWorldStartPoint().getY() < getStartPoint().getY()) &&
                   !(entry.getInWorldStartPoint().getZ() < getStartPoint().getZ()) &&
                   !(entry.getInWorldEndPoint().getX() > getEndPoint().getX()) &&
                   !(entry.getInWorldEndPoint().getY() > getEndPoint().getY()) &&
                   !(entry.getInWorldEndPoint().getZ() > getEndPoint().getZ()))
          .map(IStateEntryInfo.class::cast);
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

    @Override
    public IMultiStateSnapshot createSnapshot()
    {
        final BlockPos startBlockPos = new BlockPos(getStartPoint());
        final BlockPos endBlockPos = new BlockPos(getEndPoint());

        Stream<BlockPos> positionStream = startBlockPos.equals(endBlockPos) ? Stream.of(startBlockPos) : BlockPosStreamProvider.getForRange(
          startBlockPos.getX(), startBlockPos.getY(), startBlockPos.getZ(),
          endBlockPos.getX(), endBlockPos.getY(), endBlockPos.getZ()
        );

        final Map<BlockPos, IMultiStateSnapshot> snapshots = positionStream
          .collect(Collectors.toMap(
            Function.identity(),
            blockPos -> new ChiselAdaptingWorldMutator(getWorld(), blockPos).createSnapshot()
          ));

        return new MultiBlockMultiStateSnapshot(
          snapshots,
          getStartPoint(),
          getEndPoint()
        );
    }

    @Override
    public IWorld getWorld()
    {
        return world;
    }

    @Override
    public Vector3d getStartPoint()
    {
        return startPoint;
    }

    @Override
    public Vector3d getEndPoint()
    {
        return endPoint;
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
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0) {
            throw new IllegalArgumentException(String.format("The in area target can not have a negative component: %s", inAreaTarget.toString()));
        }

        final Vector3d actualTarget = getStartPoint().add(inAreaTarget);

        if (actualTarget.getX() > getEndPoint().getX() ||
              actualTarget.getY() > getEndPoint().getY() ||
              actualTarget.getZ() > getEndPoint().getZ()) {
            throw new IllegalArgumentException(String.format("The in area target is larger then the allowed size:%s", inAreaTarget));
        }

        final BlockPos blockPosTarget = new BlockPos(actualTarget);

        final Vector3d inBlockPosTarget = actualTarget.subtract(Vector3d.copy(blockPosTarget));
        final ChiselAdaptingWorldMutator innerMutator = new ChiselAdaptingWorldMutator(
          getWorld(), blockPosTarget
        );

        innerMutator.setInAreaTarget(blockState, inBlockPosTarget);
    }

    @Override
    public void setInBlockTarget(final BlockState blockState, final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget) throws SpaceOccupiedException
    {
        final BlockPos startPos = new BlockPos(getStartPoint());
        final BlockPos targetPos = startPos.add(inAreaBlockPosOffset);
        final Vector3d target = Vector3d.copy(targetPos).add(inBlockTarget);

        if (target.getX() < getStartPoint().getX() ||
              target.getY() < getStartPoint().getY() ||
              target.getZ() < getStartPoint().getZ()) {
            throw new IllegalArgumentException(String.format("The target can not be smaller then the start point: %s", target.toString()));
        }

        if (target.getX() > getEndPoint().getX() ||
              target.getY() > getEndPoint().getY() ||
              target.getZ() > getEndPoint().getZ()) {
            throw new IllegalArgumentException(String.format("The target can not be greater then the start point: %s", target.toString()));
        }

        final ChiselAdaptingWorldMutator innerMutator = new ChiselAdaptingWorldMutator(
          getWorld(), targetPos
        );

        innerMutator.setInBlockTarget(blockState, BlockPos.ZERO, inBlockTarget);
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
}
