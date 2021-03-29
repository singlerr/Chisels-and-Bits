package mod.chiselsandbits.multistate.snapshot;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Map;
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

    @Override
    public Stream<IStateEntryInfo> stream()
    {
        return snapshots.values()
          .stream()
          .flatMap(IAreaAccessor::stream);
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
}
