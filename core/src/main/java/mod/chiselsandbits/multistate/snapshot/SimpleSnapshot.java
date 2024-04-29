package mod.chiselsandbits.multistate.snapshot;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.axissize.CollisionType;
import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.identifier.IArrayBackedAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.callback.StateClearer;
import mod.chiselsandbits.api.multistate.mutator.callback.StateSetter;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.multistate.statistics.IMultiStateObjectStatistics;
import mod.chiselsandbits.api.util.BlockPosForEach;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.block.entities.storage.SimpleStateEntryStorage;
import mod.chiselsandbits.item.ChiseledBlockItem;
import mod.chiselsandbits.item.multistate.SingleBlockMultiStateItemStack;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleSnapshot implements IMultiStateSnapshot
{
    private final IStateEntryStorage          chunkSection;
    private       IMultiStateObjectStatistics stateObjectStatistics = null;

    public SimpleSnapshot() {
        this.chunkSection = new SimpleStateEntryStorage();

        this.chunkSection.initializeWith(BlockInformation.AIR);
    }

    public SimpleSnapshot(final IBlockInformation blockInformation) {
        this.chunkSection = new SimpleStateEntryStorage();

        this.chunkSection.initializeWith(blockInformation);
    }

    public SimpleSnapshot(final IStateEntryStorage chunkSection)
    {
        this.chunkSection = chunkSection;
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
        return new SimpleSnapshot.Identifier(this.chunkSection);
    }

    @Override
    public Stream<IStateEntryInfo> stream()
    {
        return BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
          .map(blockPos -> new SimpleSnapshot.StateEntry(
            chunkSection.getBlockInformation(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
            blockPos,
            this::setInAreaTarget,
            this::clearInAreaTarget)
          );
    }

    @Override
    public boolean isInside(final Vec3 inAreaTarget)
    {
        return !(inAreaTarget.x() < 0) &&
                 !(inAreaTarget.y() < 0) &&
                 !(inAreaTarget.z() < 0) &&
                 !(inAreaTarget.x() >= 1) &&
                 !(inAreaTarget.y() >= 1) &&
                 !(inAreaTarget.z() >= 1);
    }

    @Override
    public boolean isInside(final BlockPos inAreaBlockPosOffset, final Vec3 inBlockTarget)
    {
        if (!inAreaBlockPosOffset.equals(BlockPos.ZERO))
        {
            return false;
        }

        return isInside(inBlockTarget);
    }

    @Override
    public IMultiStateSnapshot createSnapshot()
    {
        return new SimpleSnapshot(chunkSection.createSnapshot());
    }

    @Override
    public Stream<IStateEntryInfo> streamWithPositionMutator(final IPositionMutator positionMutator)
    {
        return BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
          .map(positionMutator::mutate)
          .map(blockPos -> new SimpleSnapshot.StateEntry(
            this.chunkSection.getBlockInformation(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
            blockPos,
            this::setInAreaTarget,
            this::clearInAreaTarget)
          );
    }    /**
     * Gets the target state in the current area, using the offset from the area as well as the in area target offset.
     *
     * @param inAreaTarget The in area offset.
     * @return An optional potentially containing the state entry of the requested target.
     */
    @SuppressWarnings("deprecation")
    @Override
    public Optional<IStateEntryInfo> getInAreaTarget(final Vec3 inAreaTarget)
    {
        if (inAreaTarget.x() < 0 ||
              inAreaTarget.y() < 0 ||
              inAreaTarget.z() < 0 ||
              inAreaTarget.x() >= 1 ||
              inAreaTarget.y() >= 1 ||
              inAreaTarget.z() >= 1)
        {
            throw new IllegalArgumentException("Target is not in the current area.");
        }

        final BlockPos inAreaPos = VectorUtils.toBlockPos(inAreaTarget.multiply(StateEntrySize.current().getBitsPerBlockSide(),
          StateEntrySize.current().getBitsPerBlockSide(),
          StateEntrySize.current().getBitsPerBlockSide()));

        final IBlockInformation currentState = this.chunkSection.getBlockInformation(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ()
        );

        return currentState.isAir() ? Optional.empty() : Optional.of(new SimpleSnapshot.StateEntry(
          currentState,
          inAreaPos,
          this::setInAreaTarget,
          this::clearInAreaTarget)
        );
    }

    @Override
    public void forEachWithPositionMutator(
      final IPositionMutator positionMutator, final Consumer<IStateEntryInfo> consumer)
    {
        BlockPosForEach.forEachInRange(StateEntrySize.current().getBitsPerBlockSide(), (blockPos) -> {
            final Vec3i target = positionMutator.mutate(blockPos);
            consumer.accept(new SimpleSnapshot.StateEntry(
              this.chunkSection.getBlockInformation(target.getX(), target.getY(), target.getZ()),
              target,
              this::setInAreaTarget,
              this::clearInAreaTarget));
        });
    }

    /**
     * Returns all entries in the current area in a mutable fashion. Includes all empty areas as areas containing an air state.
     *
     * @return A stream with a mutable state entry info for each mutable section in the area.
     */
    @Override
    public Stream<IMutableStateEntryInfo> mutableStream()
    {
        return BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
          .map(blockPos -> new SimpleSnapshot.StateEntry(
            chunkSection.getBlockInformation(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
            blockPos,
            this::setInAreaTarget,
            this::clearInAreaTarget)
          );
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setInAreaTarget(
      final IBlockInformation blockInformation,
      final Vec3 inAreaTarget)
      throws SpaceOccupiedException
    {
        if (inAreaTarget.x() < 0 ||
              inAreaTarget.y() < 0 ||
              inAreaTarget.z() < 0 ||
              inAreaTarget.x() >= 1 ||
              inAreaTarget.y() >= 1 ||
              inAreaTarget.z() >= 1)
        {
            throw new IllegalArgumentException("Target is not in the current area.");
        }

        final BlockPos inAreaPos = VectorUtils.toBlockPos(inAreaTarget.multiply(StateEntrySize.current().getBitsPerBlockSide(),
          StateEntrySize.current().getBitsPerBlockSide(),
          StateEntrySize.current().getBitsPerBlockSide()));

        final IBlockInformation currentState = this.chunkSection.getBlockInformation(inAreaPos.getX(), inAreaPos.getY(), inAreaPos.getZ());
        if (!currentState.isAir())
        {
            throw new SpaceOccupiedException();
        }

        this.chunkSection.setBlockInformation(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ(),
          blockInformation
        );
    }    /**
     * Gets the target state in the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     * @return An optional potentially containing the state entry of the requested target.
     */
    @Override
    public Optional<IStateEntryInfo> getInBlockTarget(final BlockPos inAreaBlockPosOffset, final Vec3 inBlockTarget)
    {
        if (!inAreaBlockPosOffset.equals(BlockPos.ZERO))
        {
            throw new IllegalStateException(String.format("The given in area block pos offset is not inside the current block: %s", inAreaBlockPosOffset));
        }

        return this.getInAreaTarget(
          inBlockTarget
        );
    }

    @Override
    public void setInBlockTarget(final IBlockInformation blockInformation, final BlockPos inAreaBlockPosOffset, final Vec3 inBlockTarget) throws SpaceOccupiedException
    {
        if (!inAreaBlockPosOffset.equals(BlockPos.ZERO))
        {
            throw new IllegalStateException(String.format("The given in area block pos offset is not inside the current block: %s", inAreaBlockPosOffset));
        }

        this.setInAreaTarget(
          blockInformation,
          inBlockTarget);
    }

    /**
     * Clears the current area, using the offset from the area as well as the in area target offset.
     *
     * @param inAreaTarget The in area offset.
     */
    @Override
    public void clearInAreaTarget(final Vec3 inAreaTarget)
    {
        if (inAreaTarget.x() < 0 ||
              inAreaTarget.y() < 0 ||
              inAreaTarget.z() < 0 ||
              inAreaTarget.x() >= 1 ||
              inAreaTarget.y() >= 1 ||
              inAreaTarget.z() >= 1)
        {
            throw new IllegalArgumentException("Target is not in the current area.");
        }

        final BlockPos inAreaPos = VectorUtils.toBlockPos(inAreaTarget.multiply(StateEntrySize.current().getBitsPerBlockSide(),
          StateEntrySize.current().getBitsPerBlockSide(),
          StateEntrySize.current().getBitsPerBlockSide()));

        final IBlockInformation blockState = BlockInformation.AIR;

        this.chunkSection.setBlockInformation(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ(),
          blockState
        );
    }

    /**
     * Clears the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     */
    @Override
    public void clearInBlockTarget(final BlockPos inAreaBlockPosOffset, final Vec3 inBlockTarget)
    {
        if (!inAreaBlockPosOffset.equals(BlockPos.ZERO))
        {
            throw new IllegalStateException(String.format("The given in area block pos offset is not inside the current block: %s", inAreaBlockPosOffset));
        }

        this.clearInAreaTarget(
          inBlockTarget
        );
    }

    /**
     * Converts the current snapshot to a variant which is itemstack capable.
     *
     * @return The multistate itemstack which is the itemstack nbt representation of the current snapshot.
     */
    @Override
    public IMultiStateItemStack toItemStack()
    {
        final IBlockInformation primaryState = determinePrimaryState();
        final ChiseledBlockItem chiseledBlockItem = ModItems.CHISELED_BLOCK.get();

        return new SingleBlockMultiStateItemStack(chiseledBlockItem, this.chunkSection.createSnapshot());
    }

    @Override
    public IMultiStateObjectStatistics getStatics()
    {
        buildStatistics();

        return this.stateObjectStatistics;
    }

    private void buildStatistics()
    {
        //These are a limited set of statistics at the moment
        this.stateObjectStatistics = new IMultiStateObjectStatistics()
        {
            @Override
            public CompoundTag serializeNBT()
            {
                return new CompoundTag();
            }

            @Override
            public void deserializeNBT(final CompoundTag nbt)
            {
            }

            @Override
            public IBlockInformation getPrimaryState()
            {
                return determinePrimaryState();
            }

            @Override
            public Map<IBlockInformation, Integer> getStateCounts()
            {
                return stream().collect(Collectors.toMap(
                  IStateEntryInfo::getBlockInformation,
                  s -> 1,
                  Integer::sum
                ));
            }

            @Override
            public boolean shouldCheckWeakPower()
            {
                throw new NotImplementedException("Is a snapshot");
            }

            @Override
            public float getFullnessFactor()
            {
                throw new NotImplementedException("Is a snapshot");
            }

            @Override
            public float getSlipperiness()
            {
                throw new NotImplementedException("Is a snapshot");
            }

            @Override
            public float getLightEmissionFactor()
            {
                throw new NotImplementedException("Is a snapshot");
            }

            @Override
            public float getLightBlockingFactor()
            {
                throw new NotImplementedException("Is a snapshot");
            }

            @Override
            public float getRelativeBlockHardness(final Player player)
            {
                throw new NotImplementedException("Is a snapshot");
            }

            @Override
            public boolean canPropagateSkylight()
            {
                throw new NotImplementedException("Is a snapshot");
            }

            @Override
            public boolean canSustainGrassBelow()
            {
                throw new NotImplementedException("Is a snapshot");
            }

            @Override
            public BitSet getCollideableEntries(final CollisionType collisionType) {return BitSet.valueOf(new long[0]);}

            @Override
            public boolean isEmpty()
            {
                return !determinePrimaryState().isAir();
            }
        };
    }

    private IBlockInformation determinePrimaryState()
    {
        final Map<IBlockInformation, Integer> countMap = Maps.newHashMap();

        this.chunkSection.count(countMap::put);

        IBlockInformation maxState = BlockInformation.AIR;
        int maxCount = 0;
        for (final Map.Entry<IBlockInformation, Integer> blockStateIntegerEntry : countMap.entrySet())
        {
            if (maxCount < blockStateIntegerEntry.getValue() && !blockStateIntegerEntry.getKey().isAir())
            {
                maxState = blockStateIntegerEntry.getKey();
                maxCount = blockStateIntegerEntry.getValue();
            }
        }

        return maxState;
    }

    @Override
    public void rotate(final Direction.Axis axis, final int rotationCount)
    {
        this.chunkSection.rotate(axis, rotationCount);



        buildStatistics();
    }

    @Override
    public void mirror(final Direction.Axis axis)
    {
        this.chunkSection.mirror(axis);
        buildStatistics();
    }

    @Override
    public IMultiStateSnapshot clone()
    {
        return new SimpleSnapshot(
          this.chunkSection.createSnapshot()
        );
    }

    @Override
    public @NotNull AABB getBoundingBox()
    {
        return new AABB(0,0,0,1,1,1);
    }

    private static class StateEntry implements IMutableStateEntryInfo
    {
        private final IBlockInformation blockInformation;
        private final Vec3             startPoint;
        private final Vec3         endPoint;
        private final StateSetter  stateSetter;
        private final StateClearer stateClearer;

        private StateEntry(
          final IBlockInformation blockInformation,
          final Vec3i startPoint,
          final StateSetter stateSetter,
          final StateClearer stateClearer)
        {
            this.blockInformation = blockInformation;
            this.startPoint = Vec3.atLowerCornerOf(startPoint)
              .multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit());
            this.endPoint = Vec3.atLowerCornerOf(startPoint)
              .multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())
              .add(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit());
            this.stateSetter = stateSetter;
            this.stateClearer = stateClearer;
        }

        @Override
        public @NotNull IBlockInformation getBlockInformation()
        {
            return blockInformation;
        }

        @Override
        public @NotNull Vec3 getStartPoint()
        {
            return startPoint;
        }

        @Override
        public @NotNull Vec3 getEndPoint()
        {
            return endPoint;
        }

        @Override
        public void setBlockInformation(final IBlockInformation blockInformation) throws SpaceOccupiedException
        {
            stateSetter.set(blockInformation, getStartPoint());
        }

        @Override
        public void clear()
        {
            stateClearer.accept(getStartPoint());
        }
    }

    private static class Identifier implements IArrayBackedAreaShapeIdentifier
    {
        private final IStateEntryStorage snapshot;

        private Identifier(final IStateEntryStorage section)
        {
            this.snapshot = section.createSnapshot();
        }

        @Override
        public int hashCode()
        {
            return snapshot.hashCode();
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof final IArrayBackedAreaShapeIdentifier that))
            {
                return false;
            }
            return Arrays.equals(this.getBackingData(), that.getBackingData()) && that.getPalette().equals(this.getPalette());
        }

        @Override
        public long[] getBackingData()
        {
            return snapshot.getRawData();
        }

        @Override
        public List<IBlockInformation> getPalette()
        {
            return snapshot.getContainedPalette();
        }
    }




}
