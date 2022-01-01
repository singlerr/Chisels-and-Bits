package mod.chiselsandbits.multistate.snapshot;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.identifier.ILongArrayBackedAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.callback.StateClearer;
import mod.chiselsandbits.api.multistate.mutator.callback.StateSetter;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.multistate.statistics.IMultiStateObjectStatistics;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.item.ChiseledBlockItem;
import mod.chiselsandbits.item.multistate.SingleBlockMultiStateItemStack;
import mod.chiselsandbits.materials.MaterialManager;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LazilyDecodingSingleBlockMultiStateSnapshot implements IMultiStateSnapshot
{

    private Tag     lazyNbtCompound;
    private boolean loaded                = false;
    private LevelChunkSection                lazyChunkSection      = new LevelChunkSection(0, BuiltinRegistries.BIOME);
    private IMultiStateObjectStatistics stateObjectStatistics = null;

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
                   lazyChunkSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                   blockPos,
                   this::setInAreaTarget, this::clearInAreaTarget)
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

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.multiply(StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide()));

        load();

        final BlockState currentState = this.lazyChunkSection.getBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ()
        );

        return currentState.isAir() ? Optional.empty() : Optional.of(new StateEntry(
          currentState,
          inAreaPos,
          this::setInAreaTarget,
          this::clearInAreaTarget)
        );
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
    public IMultiStateSnapshot createSnapshot()
    {
        if (!loaded)
        {
            final Tag copyNbtCompound = lazyNbtCompound.copy();
            return new LazilyDecodingSingleBlockMultiStateSnapshot(copyNbtCompound);
        }

        return MultiStateSnapshotUtils.createFromSection(this.lazyChunkSection);
    }

    @Override
    public Stream<IStateEntryInfo> streamWithPositionMutator(final IPositionMutator positionMutator)
    {
        load();

        return BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
                 .map(positionMutator::mutate)
                 .map(blockPos -> new StateEntry(
                   this.lazyChunkSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                   blockPos,
                   this::setInAreaTarget,
                   this::clearInAreaTarget)
                 );
    }

    private void load()
    {
        if (this.loaded)
        {
            return;
        }

        ChunkSectionUtils.deserializeNBT(this.lazyChunkSection, this.lazyNbtCompound);
        this.loaded = true;
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
                   lazyChunkSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                   blockPos,
                   this::setInAreaTarget, this::clearInAreaTarget)
                 );
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setInAreaTarget(final BlockState blockState, final Vec3 inAreaTarget) throws SpaceOccupiedException
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

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.multiply(StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide()));

        load();

        final BlockState currentState = this.lazyChunkSection.getBlockState(inAreaPos.getX(), inAreaPos.getY(), inAreaPos.getZ());
        if (!currentState.isAir())
        {
            throw new SpaceOccupiedException();
        }

        this.lazyChunkSection.setBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ(),
          blockState,
          true
        );
    }

    @Override
    public void setInBlockTarget(final BlockState blockState, final BlockPos inAreaBlockPosOffset, final Vec3 inBlockTarget) throws SpaceOccupiedException
    {
        if (!inAreaBlockPosOffset.equals(BlockPos.ZERO))
        {
            throw new IllegalStateException(String.format("The given in area block pos offset is not inside the current block: %s", inAreaBlockPosOffset));
        }

        this.setInAreaTarget(
          blockState,
          inBlockTarget
        );
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

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.multiply(StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide()));

        load();

        final BlockState blockState = Blocks.AIR.defaultBlockState();

        this.lazyChunkSection.setBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ(),
          blockState,
          true
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
        final BlockState primaryState = determinePrimaryState();
        final Material blockMaterial = primaryState.getMaterial();
        final Material conversionMaterial = MaterialManager.getInstance().remapMaterialIfNeeded(blockMaterial);

        final Supplier<ChiseledBlockItem> convertedItemProvider = ModItems.MATERIAL_TO_ITEM_CONVERSIONS.getOrDefault(conversionMaterial, ModItems.MATERIAL_TO_ITEM_CONVERSIONS.get(Material.STONE));
        final ChiseledBlockItem chiseledBlockItem = convertedItemProvider.get();

        return new SingleBlockMultiStateItemStack(chiseledBlockItem, ChunkSectionUtils.cloneSection(this.lazyChunkSection));
    }

    @Override
    public IMultiStateObjectStatistics getStatics()
    {
        load();
        buildStatistics();

        return this.stateObjectStatistics;
    }

    @Override
    public void rotate(final Direction.Axis axis, final int rotationCount)
    {
        load();
        this.lazyChunkSection = ChunkSectionUtils.rotate90Degrees(
          this.lazyChunkSection,
          axis,
          rotationCount
        );
        this.lazyNbtCompound = ChunkSectionUtils.serializeNBT(this.lazyChunkSection);
        buildStatistics();
    }

    @Override
    public void mirror(final Direction.Axis axis)
    {
        load();
        this.lazyChunkSection = ChunkSectionUtils.mirror(
          this.lazyChunkSection,
          axis
        );
        this.lazyNbtCompound = ChunkSectionUtils.serializeNBT(this.lazyChunkSection);
        buildStatistics();
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
            public BlockState getPrimaryState()
            {
                return determinePrimaryState();
            }

            @Override
            public boolean isEmpty()
            {
                return !determinePrimaryState().isAir();
            }

            @Override
            public Map<BlockState, Integer> getStateCounts()
            {
                return stream().collect(Collectors.toMap(
                  IStateEntryInfo::getState,
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
        };
    }

    private BlockState determinePrimaryState()
    {
        final Map<BlockState, Integer> countMap = Maps.newHashMap();

        load();

        this.lazyChunkSection.getStates().count(countMap::put);

        BlockState maxState = Blocks.AIR.defaultBlockState();
        int maxCount = 0;
        for (final Map.Entry<BlockState, Integer> blockStateIntegerEntry : countMap.entrySet())
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
    public IMultiStateSnapshot clone()
    {
        load();
        return new LazilyDecodingSingleBlockMultiStateSnapshot(
          ChunkSectionUtils.serializeNBT(this.lazyChunkSection)
        );
    }

    private static class StateEntry implements IMutableStateEntryInfo
    {

        private final BlockState   blockState;
        private final Vec3     startPoint;
        private final Vec3     endPoint;
        private final StateSetter  stateSetter;
        private final StateClearer stateClearer;

        private StateEntry(
          final BlockState blockState,
          final Vec3i startPoint,
          final StateSetter stateSetter,
          final StateClearer stateClearer)
        {
            this.blockState = blockState;
            this.startPoint = Vec3.atLowerCornerOf(startPoint).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit());
            this.endPoint = Vec3.atLowerCornerOf(startPoint).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit()).add(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit());
            this.stateSetter = stateSetter;
            this.stateClearer = stateClearer;
        }

        @Override
        public BlockState getState()
        {
            return blockState;
        }

        @Override
        public Vec3 getStartPoint()
        {
            return startPoint;
        }

        @Override
        public Vec3 getEndPoint()
        {
            return endPoint;
        }

        /**
         * Sets the current entries state.
         *
         * @param blockState The new blockstate of the entry.
         */
        @Override
        public void setState(final BlockState blockState) throws SpaceOccupiedException
        {
            stateSetter.accept(blockState, getStartPoint());
        }

        /**
         * Clears the current state entries blockstate. Effectively setting the current blockstate to air.
         */
        @Override
        public void clear()
        {
            stateClearer.accept(getStartPoint());
        }
    }

    private static class Identifier implements ILongArrayBackedAreaShapeIdentifier
    {
        private final long[] identifyingPayload;

        private Identifier(final LevelChunkSection section)
        {
            this.identifyingPayload = Arrays.copyOf(
              section.getStates().data.storage().getRaw(),
              section.getStates().data.storage().getRaw().length
            );
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode(identifyingPayload);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof final ILongArrayBackedAreaShapeIdentifier that))
            {
                return false;
            }
            return Arrays.equals(identifyingPayload, that.getBackingData());
        }

        @Override
        public long[] getBackingData()
        {
            return identifyingPayload;
        }
    }




}
