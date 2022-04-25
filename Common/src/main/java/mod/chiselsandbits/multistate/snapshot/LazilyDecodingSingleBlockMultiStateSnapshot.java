package mod.chiselsandbits.multistate.snapshot;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.axissize.CollisionType;
import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
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
import mod.chiselsandbits.materials.MaterialManager;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LazilyDecodingSingleBlockMultiStateSnapshot implements IMultiStateSnapshot
{

    private final IStateEntryStorage          lazyChunkSection      = new SimpleStateEntryStorage();
    private       Tag                         lazyNbtCompound;
    private       boolean                     loaded                = false;
    private       IMultiStateObjectStatistics stateObjectStatistics = null;

    public LazilyDecodingSingleBlockMultiStateSnapshot(final Tag lazyNbtCompound) {this.lazyNbtCompound = lazyNbtCompound;}

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
        load();
        return new Identifier(this.lazyChunkSection);
    }

    @Override
    public Stream<IStateEntryInfo> stream()
    {
        load();

        return BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
          .map(blockPos -> new StateEntry(
            lazyChunkSection.getBlockInformation(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
            blockPos,
            this::setInAreaTarget,
            this::clearInAreaTarget)
          );
    }

    /**
     * Indicates if the given target is inside of the current accessor.
     *
     * @param inAreaTarget The area target to check.
     * @return True when inside, false when not.
     */
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

    /**
     * Indicates if the given target (with the given block position offset) is inside of the current accessor.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     * @return True when inside, false when not.
     */
    @Override
    public boolean isInside(final BlockPos inAreaBlockPosOffset, final Vec3 inBlockTarget)
    {
        if (!inAreaBlockPosOffset.equals(BlockPos.ZERO))
        {
            return false;
        }

        return isInside(inBlockTarget);
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

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.multiply(StateEntrySize.current().getBitsPerBlockSide(),
          StateEntrySize.current().getBitsPerBlockSide(),
          StateEntrySize.current().getBitsPerBlockSide()));

        load();

        final BlockInformation currentInformation = this.lazyChunkSection.getBlockInformation(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ()
        );

        return currentInformation.isAir() ? Optional.empty() : Optional.of(new StateEntry(
          currentInformation,
          inAreaPos,
          this::setInAreaTarget,
          this::clearInAreaTarget)
        );
    }

    @Override
    public IMultiStateSnapshot createSnapshot()
    {
        if (!loaded)
        {
            final Tag copyNbtCompound = lazyNbtCompound.copy();
            return new LazilyDecodingSingleBlockMultiStateSnapshot(copyNbtCompound);
        }

        return MultiStateSnapshotUtils.createFromStorage(this.lazyChunkSection);
    }

    @Override
    public Stream<IStateEntryInfo> streamWithPositionMutator(final IPositionMutator positionMutator)
    {
        load();

        return BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
          .map(positionMutator::mutate)
          .map(blockPos -> new StateEntry(
            this.lazyChunkSection.getBlockInformation(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
            blockPos,
            this::setInAreaTarget,
            this::clearInAreaTarget)
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
    public void forEachWithPositionMutator(
      final IPositionMutator positionMutator, final Consumer<IStateEntryInfo> consumer)
    {
        load();

        BlockPosForEach.forEachInRange(StateEntrySize.current().getBitsPerBlockSide(), (blockPos) -> {
            final Vec3i target = positionMutator.mutate(blockPos);
            consumer.accept(new StateEntry(
              this.lazyChunkSection.getBlockInformation(target.getX(), target.getY(), target.getZ()),
              target,
              this::setInAreaTarget,
              this::clearInAreaTarget));
        });
    }

    private void load()
    {
        if (this.loaded)
        {
            return;
        }

        if (this.lazyNbtCompound instanceof CompoundTag compoundTag)
        {
            this.lazyChunkSection.deserializeNBT(compoundTag);
            this.loaded = true;
        }
    }

    /**
     * Returns all entries in the current area in a mutable fashion. Includes all empty areas as areas containing an air state.
     *
     * @return A stream with a mutable state entry info for each mutable section in the area.
     */
    @Override
    public Stream<IMutableStateEntryInfo> mutableStream()
    {
        load();

        return BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
          .map(blockPos -> new StateEntry(
            lazyChunkSection.getBlockInformation(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
            blockPos,
            this::setInAreaTarget,
            this::clearInAreaTarget)
          );
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setInAreaTarget(
      final BlockInformation blockState,
      final Vec3 inAreaTarget) throws SpaceOccupiedException
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

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.multiply(StateEntrySize.current().getBitsPerBlockSide(),
          StateEntrySize.current().getBitsPerBlockSide(),
          StateEntrySize.current().getBitsPerBlockSide()));

        load();

        final BlockInformation currentInformation = this.lazyChunkSection.getBlockInformation(inAreaPos.getX(), inAreaPos.getY(), inAreaPos.getZ());
        if (!currentInformation.isAir())
        {
            throw new SpaceOccupiedException();
        }

        this.lazyChunkSection.setBlockInformation(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ(),
          blockState
        );
    }

    @Override
    public void setInBlockTarget(final BlockInformation blockInformation, final BlockPos inAreaBlockPosOffset, final Vec3 inBlockTarget) throws SpaceOccupiedException
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

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.multiply(StateEntrySize.current().getBitsPerBlockSide(),
          StateEntrySize.current().getBitsPerBlockSide(),
          StateEntrySize.current().getBitsPerBlockSide()));

        load();

        final BlockInformation airState = BlockInformation.AIR;

        this.lazyChunkSection.setBlockInformation(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ(),
          airState
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
        load();
        final BlockInformation primaryState = determinePrimaryState();
        final Material blockMaterial = primaryState.getBlockState().getMaterial();
        final Material conversionMaterial = MaterialManager.getInstance().remapMaterialIfNeeded(blockMaterial);

        final Supplier<ChiseledBlockItem> convertedItemProvider =
          ModItems.MATERIAL_TO_ITEM_CONVERSIONS.getOrDefault(conversionMaterial, ModItems.MATERIAL_TO_ITEM_CONVERSIONS.get(Material.STONE));
        final ChiseledBlockItem chiseledBlockItem = convertedItemProvider.get();

        return new SingleBlockMultiStateItemStack(chiseledBlockItem, this.lazyChunkSection.createSnapshot());
    }

    @Override
    public IMultiStateObjectStatistics getStatics()
    {
        load();
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
            public BlockInformation getPrimaryState()
            {
                return determinePrimaryState();
            }

            @Override
            public Map<BlockInformation, Integer> getStateCounts()
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
            public BitSet getCollideableEntries(final CollisionType collisionType) { return BitSet.valueOf(new long[0]); }

            @Override
            public boolean isEmpty()
            {
                return !determinePrimaryState().isAir();
            }
        };
    }

    private BlockInformation determinePrimaryState()
    {
        final Map<BlockInformation, Integer> countMap = Maps.newHashMap();

        load();

        this.lazyChunkSection.count(countMap::put);

        BlockInformation maxState = BlockInformation.AIR;
        int maxCount = 0;
        for (final Map.Entry<BlockInformation, Integer> blockStateIntegerEntry : countMap.entrySet())
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
        load();
        this.lazyChunkSection.rotate(axis, rotationCount);
        this.lazyNbtCompound = this.lazyChunkSection.serializeNBT();
        buildStatistics();
    }

    @Override
    public void mirror(final Direction.Axis axis)
    {
        load();
        this.lazyChunkSection.mirror(axis);
        this.lazyNbtCompound = this.lazyChunkSection.serializeNBT();
        buildStatistics();
    }

    @Override
    public IMultiStateSnapshot clone()
    {
        load();
        return new LazilyDecodingSingleBlockMultiStateSnapshot(
          this.lazyChunkSection.serializeNBT()
        );
    }

    private static class StateEntry implements IMutableStateEntryInfo
    {

        private final BlockInformation blockInformation;
        private final Vec3             startPoint;
        private final Vec3         endPoint;
        private final StateSetter  stateSetter;
        private final StateClearer stateClearer;

        private StateEntry(
          final BlockInformation blockInformation,
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
        public @NotNull BlockInformation getBlockInformation()
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
        public void setBlockInformation(final BlockInformation blockInformation) throws SpaceOccupiedException
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
        public byte[] getBackingData()
        {
            return snapshot.getRawData();
        }

        @Override
        public List<BlockInformation> getPalette()
        {
            return snapshot.getContainedPalette();
        }
    }
}
