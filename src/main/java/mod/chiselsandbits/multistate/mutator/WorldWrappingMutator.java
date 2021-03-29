package mod.chiselsandbits.multistate.mutator;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.multistate.snapshot.MultiBlockMultiStateSnapshot;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldWrappingMutator implements IWorldAreaMutator
{

    private final IWorld   world;
    private final Vector3d startPoint;
    private final Vector3d endPoint;

    public WorldWrappingMutator(final IWorld world, final Vector3d startPoint, final Vector3d endPoint) {
        this.world = world;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
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
}
