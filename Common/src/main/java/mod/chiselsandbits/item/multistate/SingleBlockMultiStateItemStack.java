package mod.chiselsandbits.item.multistate;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.item.multistate.IStatistics;
import mod.chiselsandbits.api.item.pattern.IPatternItem;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.identifier.ILongArrayBackedAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.callback.StateClearer;
import mod.chiselsandbits.api.multistate.mutator.callback.StateSetter;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.platforms.core.util.constants.NbtConstants;
import mod.chiselsandbits.item.ChiseledBlockItem;
import mod.chiselsandbits.materials.MaterialManager;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class SingleBlockMultiStateItemStack implements IMultiStateItemStack
{

    private final ItemStack    sourceStack;
    private LevelChunkSection compressedSection;
    private final Statistics statistics = new Statistics();

    public SingleBlockMultiStateItemStack(final ItemStack sourceStack)
    {
        this.sourceStack = sourceStack;
        this.compressedSection = new LevelChunkSection(0);

        this.deserializeNBT(sourceStack.getOrCreateTagElement(NbtConstants.CHISELED_DATA));
    }

    public SingleBlockMultiStateItemStack(final Item item, final LevelChunkSection compressedSection)
    {
        if (!(item instanceof IMultiStateItem))
            throw new IllegalArgumentException("The given item is not a MultiState Item");

        this.sourceStack = new ItemStack(item);
        this.compressedSection = compressedSection;

        this.statistics.initializeFrom(this.compressedSection);

        this.sourceStack.getOrCreateTag().put(NbtConstants.CHISELED_DATA, serializeNBT());
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
        return new ShapeIdentifier(this.compressedSection);
    }

    /**
     * Gives access to a stream with the entry state info inside the accessors range.
     *
     * @return The stream with the inner states.
     */
    @Override
    public Stream<IStateEntryInfo> stream()
    {
        return BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
                 .map(blockPos -> new StateEntry(
                     this.compressedSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                     blockPos,
                     this::setInAreaTarget,
                     this::clearInAreaTarget
                   )
                 );
    }

    /**
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

        final BlockState currentState = this.compressedSection.getBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ()
        );

        return currentState.isAir() ? Optional.empty() : Optional.of(new StateEntry(
          currentState,
          inAreaPos,
          this::setInAreaTarget,
          this::clearInAreaTarget
        ));
    }

    /**
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

        return this.isInside(
          inBlockTarget
        );
    }

    /**
     * Creates a snapshot of the current state.
     *
     * @return The snapshot.
     */
    @Override
    public IMultiStateSnapshot createSnapshot()
    {
        return MultiStateSnapshotUtils.createFromSection(this.compressedSection);
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
                 .map(blockPos -> new StateEntry(
                     this.compressedSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                     blockPos,
                     this::setInAreaTarget,
                     this::clearInAreaTarget
                   )
                 );
    }

    /**
     * Sets the target state in the current area, using the offset from the area as well as the in area target offset.
     *
     * @param blockState   The blockstate.
     * @param inAreaTarget The in area offset.
     */
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

        final BlockState currentState = this.compressedSection.getBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ()
        );

        if (!currentState.isAir())
        {
            throw new SpaceOccupiedException();
        }

        this.compressedSection.setBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ(),
          blockState,
          true
        );

        if (blockState.isAir() && !currentState.isAir()) {
            statistics.onBlockStateRemoved(currentState);
        } else if (!blockState.isAir() && currentState.isAir()) {
            statistics.onBlockStateAdded(blockState);
        } else if (!blockState.isAir() && !currentState.isAir()) {
            statistics.onBlockStateReplaced(currentState, blockState);
        }

        this.sourceStack.getOrCreateTag().put(NbtConstants.CHISELED_DATA, serializeNBT());
    }

    /**
     * Sets the target state in the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param blockState           The blockstate.
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     */
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
    @SuppressWarnings("deprecation")
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

        final BlockState blockState = Blocks.AIR.defaultBlockState();

        final BlockState currentState = this.compressedSection.getBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ()
        );

        this.compressedSection.setBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ(),
          blockState,
          true
        );

        if (blockState.isAir() && !currentState.isAir()) {
            statistics.onBlockStateRemoved(currentState);
        } else if (!blockState.isAir() && currentState.isAir()) {
            statistics.onBlockStateAdded(blockState);
        } else if (!blockState.isAir() && !currentState.isAir()) {
            statistics.onBlockStateReplaced(currentState, blockState);
        }

        this.sourceStack.getOrCreateTag().put(NbtConstants.CHISELED_DATA, serializeNBT());
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
     * Used to write the current instances data into a packet buffer.
     *
     * @param packetBuffer The packet buffer to write into.
     */
    @Override
    public void serializeInto(@NotNull final FriendlyByteBuf packetBuffer)
    {
        compressedSection.getStates().write(packetBuffer);
    }

    /**
     * Used to read the data from the packet buffer into the current instance. Potentially overriding the data that currently already exists in the instance.
     *
     * @param packetBuffer The packet buffer to read from.
     */
    @Override
    public void deserializeFrom(@NotNull final FriendlyByteBuf packetBuffer)
    {
        compressedSection.getStates().read(packetBuffer);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag nbt = new CompoundTag();

        final CompoundTag chiselBlockData = new CompoundTag();
        final CompoundTag compressedSectionData = ChunkSectionUtils.serializeNBTCompressed(this.compressedSection);
        final CompoundTag statisticsData = this.statistics.serializeNBT();

        chiselBlockData.put(NbtConstants.COMPRESSED_STORAGE, compressedSectionData);
        chiselBlockData.put(NbtConstants.STATISTICS, statisticsData);

        nbt.put(NbtConstants.CHISEL_BLOCK_ENTITY_DATA, chiselBlockData);

        return nbt;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        final CompoundTag chiselBlockData = nbt.getCompound(NbtConstants.CHISEL_BLOCK_ENTITY_DATA);
        final CompoundTag compressedSectionData = chiselBlockData.getCompound(NbtConstants.COMPRESSED_STORAGE);
        final CompoundTag statisticsData = chiselBlockData.getCompound(NbtConstants.STATISTICS);

        ChunkSectionUtils.deserializeNBT(
          this.compressedSection,
          compressedSectionData
        );

        this.statistics.deserializeNBT(statisticsData);
    }

    /**
     * The statistics of the itemstack.
     *
     * @return The statistics.
     */
    @Override
    public IStatistics getStatistics()
    {
        return statistics;
    }

    /**
     * Converts this multistack itemstack data to an actual use able itemstack.
     *
     * @return The itemstack with the data of this multistate itemstack.
     */
    @Override
    public ItemStack toBlockStack()
    {
        if (this.sourceStack.getItem() instanceof IPatternItem)
        {
            //We were created with a pattern item, instead of the block item
            //Create a new item, and copy the nbt.
            final BlockState primaryState = statistics.getPrimaryState();
            final Material blockMaterial = primaryState.getMaterial();
            final Material conversionMaterial = MaterialManager.getInstance().remapMaterialIfNeeded(blockMaterial);

            final IRegistryObject<ChiseledBlockItem> convertedItemProvider = ModItems.MATERIAL_TO_ITEM_CONVERSIONS.get(conversionMaterial);
            final ChiseledBlockItem chiseledBlockItem = convertedItemProvider.get();

            final ItemStack blockStack = new ItemStack(chiseledBlockItem);
            blockStack.setTag(sourceStack.getOrCreateTag().copy());

            return blockStack;
        }

        return sourceStack.copy();
    }

    @Override
    public ItemStack toPatternStack()
    {
        if (this.sourceStack.getItem() instanceof IPatternItem) {
            return sourceStack.copy();
        }

        final ItemStack singleUsePatternStack = new ItemStack(ModItems.SINGLE_USE_PATTERN_ITEM.get());
        singleUsePatternStack.setTag(sourceStack.getOrCreateTag().copy());
        return singleUsePatternStack;
    }

    @Override
    public Stream<IStateEntryInfo> streamWithPositionMutator(final IPositionMutator positionMutator)
    {
        return BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
                 .map(positionMutator::mutate)
                 .map(blockPos -> new StateEntry(
                     this.compressedSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                     blockPos,
                     this::setInAreaTarget,
                     this::clearInAreaTarget
                   )
                 );
    }

    @Override
    public void rotate(final Direction.Axis axis, final int rotationCount)
    {
        this.compressedSection = ChunkSectionUtils.rotate90Degrees(
          this.compressedSection,
          axis,
          rotationCount
        );

        this.statistics.clear();

        BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
          .forEach(position -> this.statistics.onBlockStateAdded(
            this.compressedSection.getBlockState(position.getX(), position.getY(), position.getZ())
          ));

        this.sourceStack.getOrCreateTag().put(NbtConstants.CHISELED_DATA, serializeNBT());
    }

    @Override
    public void mirror(final Direction.Axis axis)
    {
        this.compressedSection = ChunkSectionUtils.mirror(
          this.compressedSection,
          axis
        );
        this.statistics.clear();

        BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
          .forEach(position -> this.statistics.onBlockStateAdded(
            this.compressedSection.getBlockState(position.getX(), position.getY(), position.getZ())
          ));

        this.sourceStack.getOrCreateTag().put(NbtConstants.CHISELED_DATA, serializeNBT());
    }

    private static final class ShapeIdentifier implements ILongArrayBackedAreaShapeIdentifier
    {
        private final long[] dataArray;

        private ShapeIdentifier(final LevelChunkSection chunkSection)
        {
            dataArray = Arrays.copyOf(
              chunkSection.getStates().storage.getRaw(),
              chunkSection.getStates().storage.getRaw().length
            );
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof ILongArrayBackedAreaShapeIdentifier))
            {
                return false;
            }
            final ILongArrayBackedAreaShapeIdentifier that = (ILongArrayBackedAreaShapeIdentifier) o;
            return Arrays.equals(dataArray, that.getBackingData());
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode(dataArray);
        }

        @Override
        public long[] getBackingData()
        {
            return dataArray;
        }
    }

    private static final class StateEntry implements IMutableStateEntryInfo
    {

        private final BlockState state;
        private final Vec3   startPoint;
        private final Vec3   endPoint;

        private final StateSetter  stateSetter;
        private final StateClearer stateClearer;

        public StateEntry(
          final BlockState state,
          final Vec3i startPoint,
          final StateSetter stateSetter,
          final StateClearer stateClearer)
        {
            this(
              state,
              Vec3.atLowerCornerOf(startPoint).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit()),
              Vec3.atLowerCornerOf(startPoint).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit()).add(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit()),
              stateSetter, stateClearer);
        }

        private StateEntry(
          final BlockState state,
          final Vec3 startPoint,
          final Vec3 endPoint,
          final StateSetter stateSetter,
          final StateClearer stateClearer)
        {
            this.state = state;
            this.startPoint = startPoint;
            this.endPoint = endPoint;
            this.stateSetter = stateSetter;
            this.stateClearer = stateClearer;
        }

        @Override
        public BlockState getState()
        {
            return state;
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

    private static final class Statistics implements IStatistics
    {

        private       BlockState               primaryState = Blocks.AIR.defaultBlockState();
        private final Map<BlockState, Integer> countMap     = Maps.newConcurrentMap();

        @Override
        public BlockState getPrimaryState()
        {
            return primaryState;
        }

        @Override
        public boolean isEmpty()
        {
            return countMap.isEmpty() || (countMap.size() == 1 && countMap.containsKey(Blocks.AIR.defaultBlockState()));
        }

        private void clear() {
            this.primaryState = Blocks.AIR.defaultBlockState();

            this.countMap.clear();
        }

        private void onBlockStateAdded(final BlockState blockState) {
            countMap.putIfAbsent(blockState, 0);
            countMap.computeIfPresent(blockState, (state, currentCount) -> currentCount + 1);
            updatePrimaryState();
        }

        private void onBlockStateRemoved(final BlockState blockState) {
            countMap.computeIfPresent(blockState, (state, currentCount) -> currentCount - 1);
            countMap.remove(blockState, 0);
            updatePrimaryState();
        }

        private void onBlockStateReplaced(final BlockState currentState, final BlockState newState) {
            countMap.computeIfPresent(currentState, (state, currentCount) -> currentCount - 1);
            countMap.remove(currentState, 0);
            countMap.putIfAbsent(newState, 0);
            countMap.computeIfPresent(newState, (state, currentCount) -> currentCount + 1);
            updatePrimaryState();
        }

        private void updatePrimaryState() {
            primaryState = this.countMap.entrySet().stream()
                             .filter(entry -> !entry.getKey().isAir())
                             .min((o1, o2) -> -1 * (o1.getValue() - o2.getValue()))
                             .map(Map.Entry::getKey)
                             .orElseGet(Blocks.AIR::defaultBlockState);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag nbt = new CompoundTag();

            nbt.put(NbtConstants.PRIMARY_STATE, NbtUtils.writeBlockState(this.primaryState));

            final ListTag blockStateList = new ListTag();
            for (final Map.Entry<BlockState, Integer> blockStateIntegerEntry : this.countMap.entrySet())
            {
                final CompoundTag stateNbt = new CompoundTag();

                stateNbt.put(NbtConstants.BLOCK_STATE, NbtUtils.writeBlockState(blockStateIntegerEntry.getKey()));
                stateNbt.putInt(NbtConstants.COUNT, blockStateIntegerEntry.getValue());

                blockStateList.add(stateNbt);
            }

            nbt.put(NbtConstants.BLOCK_STATES, blockStateList);

            return nbt;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.countMap.clear();

            this.primaryState = NbtUtils.readBlockState(nbt.getCompound(NbtConstants.PRIMARY_STATE));

            final ListTag blockStateList = nbt.getList(NbtConstants.BLOCK_STATES, Tag.TAG_COMPOUND);
            for (int i = 0; i < blockStateList.size(); i++)
            {
                final CompoundTag stateNbt = blockStateList.getCompound(i);

                this.countMap.put(
                  NbtUtils.readBlockState(stateNbt.getCompound(NbtConstants.BLOCK_STATE)),
                  stateNbt.getInt(NbtConstants.COUNT)
                );
            }
        }

        public void initializeFrom(final LevelChunkSection compressedSection)
        {
            this.clear();

            compressedSection.getStates().count(countMap::putIfAbsent);
            updatePrimaryState();
        }
    }

}
