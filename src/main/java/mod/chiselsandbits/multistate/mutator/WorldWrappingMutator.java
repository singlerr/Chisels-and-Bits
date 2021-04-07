package mod.chiselsandbits.multistate.mutator;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.world.IInWorldMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.multistate.snapshot.MultiBlockMultiStateSnapshot;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import org.intellij.lang.annotations.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldWrappingMutator implements IWorldAreaMutator
{

    private final IWorld world;
    private final Vector3d startPoint;
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
              inAreaTarget.getZ() < 0) {
            return false;
        }

        final Vector3d actualTarget = getStartPoint().add(inAreaTarget);

        return !(actualTarget.getX() >= getEndPoint().getX()) &&
                 !(actualTarget.getY() >= getEndPoint().getY()) &&
                 !(actualTarget.getZ() >= getEndPoint().getZ());
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
        final BlockPos startPos = new BlockPos(getStartPoint());
        final BlockPos targetPos = startPos.add(inAreaBlockPosOffset);
        final Vector3d target = Vector3d.copy(targetPos).add(inBlockTarget);

        return !(target.getX() < getStartPoint().getX()) &&
                 !(target.getY() < getStartPoint().getY()) &&
                 !(target.getZ() < getStartPoint().getZ()) &&
                 !(target.getX() >= getEndPoint().getX()) &&
                 !(target.getY() >= getEndPoint().getY()) &&
                 !(target.getZ() >= getEndPoint().getZ());
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
        return BlockPosStreamProvider.getForRange(
          getStartPoint(), getEndPoint()
        ).flatMap(blockPos -> positionBasedMutableStream(blockPos)
                .map(mutableEntry -> new PositionAdaptingMutableStateEntry(mutableEntry, blockPos)));
    }

    private Stream<IMutableStateEntryInfo> positionBasedMutableStream(final BlockPos position) {
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
              inAreaTarget.getZ() < 0) {
            throw new IllegalArgumentException(String.format("The in area target can not have a negative component: %s", inAreaTarget.toString()));
        }

        final Vector3d actualTarget = getStartPoint().add(inAreaTarget);

        if (actualTarget.getX() >= getEndPoint().getX() ||
              actualTarget.getY() >= getEndPoint().getY() ||
              actualTarget.getZ() >= getEndPoint().getZ()) {
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

        if (target.getX() >= getEndPoint().getX() ||
              target.getY() >= getEndPoint().getY() ||
              target.getZ() >= getEndPoint().getZ()) {
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
              inAreaTarget.getZ() < 0) {
            throw new IllegalArgumentException(String.format("The in area target can not have a negative component: %s", inAreaTarget.toString()));
        }

        final Vector3d actualTarget = getStartPoint().add(inAreaTarget);

        if (actualTarget.getX() >= getEndPoint().getX() ||
              actualTarget.getY() >= getEndPoint().getY() ||
              actualTarget.getZ() >= getEndPoint().getZ()) {
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
        final BlockPos startPos = new BlockPos(getStartPoint());
        final BlockPos targetPos = startPos.add(inAreaBlockPosOffset);
        final Vector3d target = Vector3d.copy(targetPos).add(inBlockTarget);

        if (target.getX() < getStartPoint().getX() ||
              target.getY() < getStartPoint().getY() ||
              target.getZ() < getStartPoint().getZ()) {
            throw new IllegalArgumentException(String.format("The target can not be smaller then the start point: %s", target.toString()));
        }

        if (target.getX() >= getEndPoint().getX() ||
              target.getY() >= getEndPoint().getY() ||
              target.getZ() >= getEndPoint().getZ()) {
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
          getStartPoint(), getEndPoint()
        ).flatMap(blockPos -> positionBasedInWorldMutableStream(blockPos)
                                .map(mutableEntry -> new InWorldPositionAdaptingMutableStateEntry(mutableEntry, getWorld(), blockPos)));
    }

    private Stream<IMutableStateEntryInfo> positionBasedInWorldMutableStream(final BlockPos position) {
        final ChiselAdaptingWorldMutator innerMutator = new ChiselAdaptingWorldMutator(
          getWorld(), position
        );

        return innerMutator.mutableStream();
    }

    private static final class PositionAdaptingMutableStateEntry implements IMutableStateEntryInfo {
        private final IMutableStateEntryInfo source;
        private final Vector3d offSet;

        private PositionAdaptingMutableStateEntry(
          final IMutableStateEntryInfo source,
          final BlockPos position) {
            this.source = source;
            this.offSet = Vector3d.copy(position);
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
    }

    private static final class InWorldPositionAdaptingMutableStateEntry implements IInWorldMutableStateEntryInfo {
        private final IMutableStateEntryInfo source;
        private final IWorld world;
        private final Vector3d offSet;

        private InWorldPositionAdaptingMutableStateEntry(
          final IMutableStateEntryInfo source,
          final IWorld world,
          final BlockPos position) {
            this.source = source;
            this.world = world;
            this.offSet = Vector3d.copy(position);
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
         * The world, in the form of a block reader, that this entry info resides in.
         *
         * @return The world.
         */
        @Override
        public IWorld getWorld()
        {
            return world;
        }

        /**
         * The position of the block that this state entry is part of.
         *
         * @return The in world block position.
         */
        @Override
        public BlockPos getBlockPos()
        {
            return new BlockPos(offSet);
        }
    }

}
