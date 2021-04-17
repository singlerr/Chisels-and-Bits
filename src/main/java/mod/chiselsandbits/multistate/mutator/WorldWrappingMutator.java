package mod.chiselsandbits.multistate.mutator;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.world.IInWorldMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.api.util.IWorldObject;
import mod.chiselsandbits.multistate.snapshot.MultiBlockMultiStateSnapshot;
import mod.chiselsandbits.utils.WorldObjectUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldWrappingMutator implements IWorldAreaMutator
{

    private final IWorld   world;
    private final Vector3d startPoint;
    private final Vector3d endPoint;

    public WorldWrappingMutator(final IWorld world, final Vector3d startPoint, final Vector3d endPoint)
    {
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
        final BlockPos startBlockPos = new BlockPos(getInWorldStartPoint());
        final BlockPos endBlockPos = new BlockPos(getInWorldEndPoint());

        Stream<BlockPos> positionStream = startBlockPos.equals(endBlockPos) ? Stream.of(startBlockPos) : BlockPosStreamProvider.getForRange(
          startBlockPos.getX(), startBlockPos.getY(), startBlockPos.getZ(),
          endBlockPos.getX(), endBlockPos.getY(), endBlockPos.getZ()
        );

        return positionStream.map(blockPos -> new ChiselAdaptingWorldMutator(getWorld(), blockPos))
                 .flatMap(ChiselAdaptingWorldMutator::inWorldStream)
                 .filter(entry ->  this.getBoundingBox().intersects(entry.getBoundingBox()) || entry.getBoundingBox().intersects(this.getBoundingBox()))
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

    /**
     * Indicates if the given target is inside of the current accessor.
     *
     * @param inAreaTarget The area target to check.
     * @return True when inside, false when not.
     */
    @Override
    public boolean isInside(final Vector3d inAreaTarget)
    {
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0)
        {
            return false;
        }

        final Vector3d actualTarget = getInWorldStartPoint().add(inAreaTarget);

        return !(actualTarget.getX() >= getInWorldEndPoint().getX()) &&
                 !(actualTarget.getY() >= getInWorldEndPoint().getY()) &&
                 !(actualTarget.getZ() >= getInWorldEndPoint().getZ());
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
        final BlockPos startPos = new BlockPos(getInWorldStartPoint());
        final BlockPos targetPos = startPos.add(inAreaBlockPosOffset);
        final Vector3d target = Vector3d.copy(targetPos).add(inBlockTarget);

        return !(target.getX() < getInWorldStartPoint().getX()) &&
                 !(target.getY() < getInWorldStartPoint().getY()) &&
                 !(target.getZ() < getInWorldStartPoint().getZ()) &&
                 !(target.getX() >= getInWorldEndPoint().getX()) &&
                 !(target.getY() >= getInWorldEndPoint().getY()) &&
                 !(target.getZ() >= getInWorldEndPoint().getZ());
    }

    @Override
    public IMultiStateSnapshot createSnapshot()
    {
        final BlockPos startBlockPos = new BlockPos(getInWorldStartPoint());
        final BlockPos endBlockPos = new BlockPos(getInWorldEndPoint());

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
          getInWorldStartPoint(),
          getInWorldEndPoint()
        );
    }

    @Override
    public IWorld getWorld()
    {
        return world;
    }

    @Override
    public Vector3d getInWorldStartPoint()
    {
        return startPoint;
    }

    @Override
    public Vector3d getInWorldEndPoint()
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
        return BlockPosStreamProvider.getForRange(
          getInWorldStartPoint(), getInWorldEndPoint()
        ).flatMap(blockPos -> positionBasedMutableStream(blockPos)
                                .map(mutableEntry -> new PositionAdaptingMutableStateEntry(mutableEntry, blockPos, getWorld())))
                 .filter(entry ->  this.getBoundingBox().intersects(entry.getBoundingBox()) || entry.getBoundingBox().intersects(this.getBoundingBox()))
                 .map(IMutableStateEntryInfo.class::cast);
    }

    private Stream<IMutableStateEntryInfo> positionBasedMutableStream(final BlockPos position)
    {
        final ChiselAdaptingWorldMutator innerMutator = new ChiselAdaptingWorldMutator(
          getWorld(), position
        );

        return innerMutator.mutableStream();
    }

    @Override
    public void setInAreaTarget(final BlockState blockState, final Vector3d inAreaTarget) throws SpaceOccupiedException
    {
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0)
        {
            throw new IllegalArgumentException(String.format("The in area target can not have a negative component: %s", inAreaTarget.toString()));
        }

        final Vector3d actualTarget = getInWorldStartPoint().add(inAreaTarget);

        if (actualTarget.getX() >= getInWorldEndPoint().getX() ||
              actualTarget.getY() >= getInWorldEndPoint().getY() ||
              actualTarget.getZ() >= getInWorldEndPoint().getZ())
        {
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
        final BlockPos startPos = new BlockPos(getInWorldStartPoint());
        final BlockPos targetPos = startPos.add(inAreaBlockPosOffset);
        final Vector3d target = Vector3d.copy(targetPos).add(inBlockTarget);

        if (target.getX() < getInWorldStartPoint().getX() ||
              target.getY() < getInWorldStartPoint().getY() ||
              target.getZ() < getInWorldStartPoint().getZ())
        {
            throw new IllegalArgumentException(String.format("The target can not be smaller then the start point: %s", target.toString()));
        }

        if (target.getX() >= getInWorldEndPoint().getX() ||
              target.getY() >= getInWorldEndPoint().getY() ||
              target.getZ() >= getInWorldEndPoint().getZ())
        {
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
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0)
        {
            throw new IllegalArgumentException(String.format("The in area target can not have a negative component: %s", inAreaTarget.toString()));
        }

        final Vector3d actualTarget = getInWorldStartPoint().add(inAreaTarget);

        if (actualTarget.getX() >= getInWorldEndPoint().getX() ||
              actualTarget.getY() >= getInWorldEndPoint().getY() ||
              actualTarget.getZ() >= getInWorldEndPoint().getZ())
        {
            throw new IllegalArgumentException(String.format("The in area target is larger then the allowed size:%s", inAreaTarget));
        }

        final BlockPos blockPosTarget = new BlockPos(actualTarget);

        final Vector3d inBlockPosTarget = actualTarget.subtract(Vector3d.copy(blockPosTarget));
        final ChiselAdaptingWorldMutator innerMutator = new ChiselAdaptingWorldMutator(
          getWorld(), blockPosTarget
        );

        innerMutator.clearInAreaTarget(inBlockPosTarget);
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
        final BlockPos startPos = new BlockPos(getInWorldStartPoint());
        final BlockPos targetPos = startPos.add(inAreaBlockPosOffset);
        final Vector3d target = Vector3d.copy(targetPos).add(inBlockTarget);

        if (target.getX() < getInWorldStartPoint().getX() ||
              target.getY() < getInWorldStartPoint().getY() ||
              target.getZ() < getInWorldStartPoint().getZ())
        {
            throw new IllegalArgumentException(String.format("The target can not be smaller then the start point: %s", target.toString()));
        }

        if (target.getX() >= getInWorldEndPoint().getX() ||
              target.getY() >= getInWorldEndPoint().getY() ||
              target.getZ() >= getInWorldEndPoint().getZ())
        {
            throw new IllegalArgumentException(String.format("The target can not be greater then the start point: %s", target.toString()));
        }

        final ChiselAdaptingWorldMutator innerMutator = new ChiselAdaptingWorldMutator(
          getWorld(), targetPos
        );

        innerMutator.clearInBlockTarget(BlockPos.ZERO, inBlockTarget);
    }

    /**
     * Returns all entries in the current area in a mutable fashion. Includes all empty areas as areas containing an air state.
     *
     * @return A stream with a mutable state entry info for each mutable section in the area.
     */
    @Override
    public Stream<IInWorldMutableStateEntryInfo> inWorldMutableStream()
    {
        return BlockPosStreamProvider.getForRange(
          getInWorldStartPoint(), getInWorldEndPoint()
        ).flatMap(blockPos -> positionBasedInWorldMutableStream(blockPos)
                                .filter(entry -> this.getBoundingBox().intersects(entry.getBoundingBox()) || entry.getBoundingBox().intersects(this.getBoundingBox())));
    }

    private Stream<IInWorldMutableStateEntryInfo> positionBasedInWorldMutableStream(final BlockPos position)
    {
        final ChiselAdaptingWorldMutator innerMutator = new ChiselAdaptingWorldMutator(
          getWorld(), position
        );

        return innerMutator.inWorldMutableStream();
    }

    /**
     * Trigger a batch mutation start.
     * <p>
     * As long as at least one batch mutation is still running no changes are transmitted to the client.
     *
     * @return The batch mutation lock.
     */
    @Override
    public IBatchMutation batch()
    {
        return new BatchMutationLock(
          BlockPosStreamProvider.getForRange(
            getInWorldStartPoint(), getInWorldEndPoint()
          ).map(blockPos -> new ChiselAdaptingWorldMutator(
            getWorld(), blockPos))
            .map(ChiselAdaptingWorldMutator::batch)
            .collect(Collectors.toList())
        );
    }

    private static final class PositionAdaptingMutableStateEntry implements IMutableStateEntryInfo, IWorldObject
    {
        private final IMutableStateEntryInfo source;
        private final Vector3d               offSet;
        private final IWorld                 world;

        private PositionAdaptingMutableStateEntry(
          final IMutableStateEntryInfo source,
          final BlockPos position, final IWorld world)
        {
            this.source = source;
            this.offSet = Vector3d.copy(position);
            this.world = world;
        }

        /**
         * The state that this entry represents.
         *
         * @return The state.
         */
        @Override
        public BlockState getState()
        {
            return source.getState();
        }

        /**
         * The start (lowest on all three axi) position of the state that this entry occupies.
         *
         * @return The start position of this entry in the given block.
         */
        @Override
        public Vector3d getStartPoint()
        {
            return source.getStartPoint().add(offSet);
        }

        /**
         * The end (highest on all three axi) position of the state that this entry occupies.
         *
         * @return The start position of this entry in the given block.
         */
        @Override
        public Vector3d getEndPoint()
        {
            return source.getEndPoint().add(offSet);
        }

        /**
         * Sets the current entries state.
         *
         * @param blockState The new blockstate of the entry.
         */
        @Override
        public void setState(final BlockState blockState) throws SpaceOccupiedException
        {
            source.setState(blockState);
        }

        /**
         * Clears the current state entries blockstate. Effectively setting the current blockstate to air.
         */
        @Override
        public void clear()
        {
            source.clear();
        }

        /**
         * The world the object is in.
         *
         * @return The world.
         */
        @Override
        public IWorld getWorld()
        {
            return world;
        }

        /**
         * The start point of the object in the world.
         *
         * @return The start point.
         */
        @Override
        public Vector3d getInWorldStartPoint()
        {
            return getStartPoint();
        }

        /**
         * The end point of the object in the world.
         *
         * @return The end point.
         */
        @Override
        public Vector3d getInWorldEndPoint()
        {
            return getEndPoint();
        }
    }

    private static final class BatchMutationLock implements IBatchMutation
    {

        private final Iterable<IBatchMutation> innerLocks;

        private BatchMutationLock(final Iterable<IBatchMutation> innerLocks) {this.innerLocks = innerLocks;}

        @Override
        public void close()
        {
            for (IBatchMutation innerLock : innerLocks)
            {
                innerLock.close();
            }
        }
    }
}
