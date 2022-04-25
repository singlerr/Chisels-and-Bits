package mod.chiselsandbits.multistate.accessor;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.block.entities.storage.SimpleStateEntryStorage;
import mod.chiselsandbits.multistate.snapshot.SimpleSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SingleBlockStateAccessor implements IAreaAccessor
{
    public static final SingleBlockStateAccessor AIR = new SingleBlockStateAccessor(BlockInformation.AIR);

    private final BlockInformation blockInformation;

    public SingleBlockStateAccessor(final BlockInformation blockInformation) {this.blockInformation = blockInformation;}

    @Override
    public IAreaShapeIdentifier createNewShapeIdentifier()
    {
        return new Identifier(blockInformation);
    }

    @Override
    public Stream<IStateEntryInfo> stream()
    {
        return Stream.of(new StateEntryInfo(blockInformation));
    }

    @Override
    public boolean isInside(final Vec3 inAreaTarget)
    {
        return inAreaTarget.x() >= 0 && inAreaTarget.y() >= 0 && inAreaTarget.z() >= 0
          && inAreaTarget.x() < 1 && inAreaTarget.y() < 1 && inAreaTarget.z() < 1;
    }

    @Override
    public boolean isInside(final BlockPos inAreaBlockPosOffset, final Vec3 inBlockTarget)
    {
        return inAreaBlockPosOffset.equals(BlockPos.ZERO) && isInside(inBlockTarget);
    }

    @Override
    public IMultiStateSnapshot createSnapshot()
    {
        return new SimpleSnapshot(blockInformation);
    }

    @Override
    public Stream<IStateEntryInfo> streamWithPositionMutator(final IPositionMutator positionMutator)
    {
        return null;
    }

    @Override
    public void forEachWithPositionMutator(
      final IPositionMutator positionMutator, final Consumer<IStateEntryInfo> consumer)
    {

    }

    @Override
    public Optional<IStateEntryInfo> getInAreaTarget(final Vec3 inAreaTarget)
    {
        return Optional.empty();
    }

    @Override
    public Optional<IStateEntryInfo> getInBlockTarget(final BlockPos inAreaBlockPosOffset, final Vec3 inBlockTarget)
    {
        return Optional.empty();
    }

    private static final class Identifier implements IAreaShapeIdentifier {
        private final BlockInformation blockInformation;

        private Identifier(final BlockInformation blockInformation) {this.blockInformation = blockInformation;}

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof final Identifier that))
            {
                return false;
            }

            return Objects.equals(blockInformation, that.blockInformation);
        }

        @Override
        public int hashCode()
        {
            return blockInformation != null ? blockInformation.hashCode() : 0;
        }
    }

    private static final class StateEntryInfo implements IStateEntryInfo {

        private static final Vec3 START = Vec3.ZERO;
        private static final Vec3 END = Vec3.ZERO.add(1,1,1);

        private final BlockInformation blockInformation;

        private StateEntryInfo(final BlockInformation blockInformation) {this.blockInformation = blockInformation;}

        @Override
        public @NotNull BlockInformation getBlockInformation()
        {
            return blockInformation;
        }

        @Override
        public @NotNull Vec3 getStartPoint()
        {
            return START;
        }

        @Override
        public @NotNull Vec3 getEndPoint()
        {
            return END;
        }
    }
}
