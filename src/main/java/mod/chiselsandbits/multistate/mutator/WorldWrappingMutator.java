package mod.chiselsandbits.multistate.mutator;

import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessorWithVoxelShape;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.world.IInWorldMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.api.util.IWorldObject;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
import mod.chiselsandbits.multistate.snapshot.MultiBlockMultiStateSnapshot;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldWrappingMutator implements IWorldAreaMutator, IAreaAccessorWithVoxelShape
{

    private final IWorld   world;
    private final Vector3d startPoint;
    private final Vector3d endPoint;

    public WorldWrappingMutator(final IWorld world, final Vector3d startPoint, final Vector3d endPoint)
    {
        this.world = world;
        this.startPoint = new Vector3d(
          Math.min(startPoint.x(), endPoint.x()),
          Math.min(startPoint.y(), endPoint.y()),
          Math.min(startPoint.z(), endPoint.z())
        );
        this.endPoint = new Vector3d(
          Math.max(startPoint.x(), endPoint.x()),
          Math.max(startPoint.y(), endPoint.y()),
          Math.max(startPoint.z(), endPoint.z())
        );
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
        final BlockPos startBlockPos = new BlockPos(getInWorldStartPoint());
        final BlockPos endBlockPos = new BlockPos(getInWorldEndPoint());
        Stream<BlockPos> positionStream = startBlockPos.equals(endBlockPos) ? Stream.of(startBlockPos) : BlockPosStreamProvider.getForRange(
          startBlockPos.getX(), startBlockPos.getY(), startBlockPos.getZ(),
          endBlockPos.getX(), endBlockPos.getY(), endBlockPos.getZ()
        );

        final Collection<IAreaShapeIdentifier> identifiers = positionStream.map(blockPos -> new ChiselAdaptingWorldMutator(getWorld(), blockPos))
                                                               .map(ChiselAdaptingWorldMutator::createNewShapeIdentifier)
                                                               .collect(Collectors.toList());

        return new Identifier(
          identifiers,
          startPoint, endPoint);
    }

    @Override
    public Stream<IStateEntryInfo> stream()
    {
        return inWorldMutableStream()
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
        if (!isInside(inAreaTarget))
            return Optional.empty();

        final BlockPos exactPosition = new BlockPos(inAreaTarget);
        final Vector3d exactInBlockOffset = inAreaTarget.subtract(
          exactPosition.getX(),
          exactPosition.getY(),
          exactPosition.getZ()
        );

        return getInBlockTarget(exactPosition, exactInBlockOffset);
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
        if (!isInside(inAreaBlockPosOffset, inBlockTarget))
            return Optional.empty();

        return new ChiselAdaptingWorldMutator(getWorld(), inAreaBlockPosOffset)
                                                     .getInAreaTarget(inBlockTarget);
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
        return getInWorldBoundingBox().contains(inAreaTarget);
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
        return isInside(Vector3d.atLowerCornerOf(inAreaBlockPosOffset).add(inBlockTarget));
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
    public Stream<IStateEntryInfo> streamWithPositionMutator(final IPositionMutator positionMutator)
    {
        return BlockPosStreamProvider.getForRange(
          getInWorldStartPoint().multiply(StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide()),
          getInWorldEndPoint().multiply(StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide())
        )
                 .map(positionMutator::mutate)
                 .map(position -> Vector3d.atLowerCornerOf(position).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit()))
                 .map(position -> {
                     final BlockPos blockPos = new BlockPos(position);
                     final Vector3d inBlockOffset = position.subtract(Vector3d.atLowerCornerOf(blockPos));

                     return getInBlockTarget(blockPos, inBlockOffset);
                 })
                 .filter(Optional::isPresent)
                 .map(Optional::get);
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
        return inWorldMutableStream()
                 .map(IMutableStateEntryInfo.class::cast);
    }

    @Override
    public void setInAreaTarget(final BlockState blockState, final Vector3d inAreaTarget) throws SpaceOccupiedException
    {
        if (inAreaTarget.x() < 0 ||
              inAreaTarget.y() < 0 ||
              inAreaTarget.z() < 0)
        {
            throw new IllegalArgumentException(String.format("The in area target can not have a negative component: %s", inAreaTarget));
        }

        final Vector3d actualTarget = getInWorldStartPoint().add(inAreaTarget);

        if (actualTarget.x() >= getInWorldEndPoint().x() ||
              actualTarget.y() >= getInWorldEndPoint().y() ||
              actualTarget.z() >= getInWorldEndPoint().z())
        {
            throw new IllegalArgumentException(String.format("The in area target is larger then the allowed size:%s", inAreaTarget));
        }

        final BlockPos blockPosTarget = new BlockPos(actualTarget);

        final Vector3d inBlockPosTarget = actualTarget.subtract(Vector3d.atLowerCornerOf(blockPosTarget));
        final ChiselAdaptingWorldMutator innerMutator = new ChiselAdaptingWorldMutator(
          getWorld(), blockPosTarget
        );

        innerMutator.setInAreaTarget(blockState, inBlockPosTarget);
    }

    @Override
    public void setInBlockTarget(final BlockState blockState, final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget) throws SpaceOccupiedException
    {
        final BlockPos startPos = new BlockPos(getInWorldStartPoint());
        final BlockPos targetPos = startPos.offset(inAreaBlockPosOffset);
        final Vector3d target = Vector3d.atLowerCornerOf(targetPos).add(inBlockTarget);

        if (target.x() < getInWorldStartPoint().x() ||
              target.y() < getInWorldStartPoint().y() ||
              target.z() < getInWorldStartPoint().z())
        {
            throw new IllegalArgumentException(String.format("The target can not be smaller then the start point: %s", target));
        }

        if (target.x() >= getInWorldEndPoint().x() ||
              target.y() >= getInWorldEndPoint().y() ||
              target.z() >= getInWorldEndPoint().z())
        {
            throw new IllegalArgumentException(String.format("The target can not be greater then the start point: %s", target));
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
        if (inAreaTarget.x() < 0 ||
              inAreaTarget.y() < 0 ||
              inAreaTarget.z() < 0)
        {
            throw new IllegalArgumentException(String.format("The in area target can not have a negative component: %s", inAreaTarget));
        }

        final Vector3d actualTarget = getInWorldStartPoint().add(inAreaTarget);

        if (actualTarget.x() >= getInWorldEndPoint().x() ||
              actualTarget.y() >= getInWorldEndPoint().y() ||
              actualTarget.z() >= getInWorldEndPoint().z())
        {
            throw new IllegalArgumentException(String.format("The in area target is larger then the allowed size:%s", inAreaTarget));
        }

        final BlockPos blockPosTarget = new BlockPos(actualTarget);

        final Vector3d inBlockPosTarget = actualTarget.subtract(Vector3d.atLowerCornerOf(blockPosTarget));
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
        final BlockPos targetPos = startPos.offset(inAreaBlockPosOffset);
        final Vector3d target = Vector3d.atLowerCornerOf(targetPos).add(inBlockTarget);

        if (target.x() < getInWorldStartPoint().x() ||
              target.y() < getInWorldStartPoint().y() ||
              target.z() < getInWorldStartPoint().z())
        {
            throw new IllegalArgumentException(String.format("The target can not be smaller then the start point: %s", target));
        }

        if (target.x() >= getInWorldEndPoint().x() ||
              target.y() >= getInWorldEndPoint().y() ||
              target.z() >= getInWorldEndPoint().z())
        {
            throw new IllegalArgumentException(String.format("The target can not be greater then the start point: %s", target));
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
                                .filter(entry -> this.getInWorldBoundingBox().intersects(entry.getInWorldBoundingBox()) || entry.getInWorldBoundingBox().intersects(this.getInWorldBoundingBox())));
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

    @Override
    public IBatchMutation batch(final IChangeTracker changeTracker)
    {
        final Map<BlockPos, IMultiStateSnapshot> before = BlockPosStreamProvider.getForRange(
            getInWorldStartPoint(), getInWorldEndPoint()
          ).map(blockPos -> new ChiselAdaptingWorldMutator(
            getWorld(), blockPos))
          .collect(Collectors.toMap(
            ChiselAdaptingWorldMutator::getPos,
            ChiselAdaptingWorldMutator::createSnapshot
          ));

        final IBatchMutation innerMutation = batch();
        return () -> {
            final Map<BlockPos, IMultiStateSnapshot> after = BlockPosStreamProvider.getForRange(
                getInWorldStartPoint(), getInWorldEndPoint()
              ).map(blockPos -> new ChiselAdaptingWorldMutator(
                getWorld(), blockPos))
              .collect(Collectors.toMap(
                ChiselAdaptingWorldMutator::getPos,
                ChiselAdaptingWorldMutator::createSnapshot
              ));

            innerMutation.close();
            changeTracker.onBlocksUpdated(
              before, after
            );
        };
    }

    @Override
    public VoxelShape provideShape(final Function<IAreaAccessor, Predicate<IStateEntryInfo>> selectablePredicateBuilder, final BlockPos offset, final boolean simplify)
    {
        final VoxelShape areaShape = VoxelShapes.create(getInWorldBoundingBox().move(VectorUtils.invert(new BlockPos(getInWorldStartPoint()))).move(offset));
        final VoxelShape containedShape = BlockPosStreamProvider.getForRange(
          getInWorldStartPoint(), getInWorldEndPoint()
        ).map(blockPos -> new ChiselAdaptingWorldMutator(
          getWorld(), blockPos))
                                            .map(a -> IVoxelShapeManager.getInstance().get(a, new BlockPos(a.getInWorldStartPoint()).offset(VectorUtils.invert(new BlockPos(getInWorldStartPoint()))).offset(offset),
                                              selectablePredicateBuilder, simplify))
                                            .reduce(
                                              VoxelShapes.empty(),
                                              (voxelShape, bbShape) -> VoxelShapes.joinUnoptimized(voxelShape, bbShape, IBooleanFunction.OR),
                                              (voxelShape, voxelShape2) -> VoxelShapes.joinUnoptimized(voxelShape, voxelShape2, IBooleanFunction.OR)
                                            );
        final VoxelShape requestedShape = VoxelShapes.joinUnoptimized(
          areaShape,
          containedShape,
          IBooleanFunction.AND
        );

        return simplify ? requestedShape.optimize() : requestedShape;
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

    private static final class Identifier implements IAreaShapeIdentifier {
        private final Collection<IAreaShapeIdentifier> inners;
        private final Vector3d startPoint;
        private final Vector3d endPoint;

        public Identifier(final Collection<IAreaShapeIdentifier> innerSnapshots, final Vector3d startPoint, final Vector3d endPoint)
        {
            this.inners = innerSnapshots;
            this.startPoint = startPoint;
            this.endPoint = endPoint;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof Identifier))
            {
                return false;
            }
            final Identifier that = (Identifier) o;
            return inners.equals(that.inners) && startPoint.equals(that.startPoint) && endPoint.equals(that.endPoint);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(inners, startPoint, endPoint);
        }
    }
}
