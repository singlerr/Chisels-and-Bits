package mod.chiselsandbits.item.multistate;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.item.multistate.IStatistics;
import mod.chiselsandbits.api.multistate.accessor.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.callback.StateClearer;
import mod.chiselsandbits.api.multistate.mutator.callback.StateSetter;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.*;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.BITS_PER_BLOCK_SIDE;
import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.SIZE_PER_BIT;

public class ChiseledBlockMultiStateItemStack implements IMultiStateItemStack
{

    private final ItemStack    sourceStack;
    private final ChunkSection compressedSection;
    private final Statistics statistics = new Statistics();

    public ChiseledBlockMultiStateItemStack(final ItemStack sourceStack)
    {
        this.sourceStack = sourceStack;
        this.compressedSection = new ChunkSection(0);

        this.deserializeNBT(sourceStack.getOrCreateChildTag(NbtConstants.CHISELED_DATA));
    }

    public ChiseledBlockMultiStateItemStack(final Item item, final ChunkSection compressedSection)
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
        return BlockPosStreamProvider.getForRange(BITS_PER_BLOCK_SIDE)
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
    public Optional<IStateEntryInfo> getInAreaTarget(final Vector3d inAreaTarget)
    {
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0 ||
              inAreaTarget.getX() >= 1 ||
              inAreaTarget.getY() >= 1 ||
              inAreaTarget.getZ() >= 1)
        {
            throw new IllegalArgumentException("Target is not in the current area.");
        }

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE));

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
    public Optional<IStateEntryInfo> getInBlockTarget(final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget)
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
    public boolean isInside(final Vector3d inAreaTarget)
    {
        return !(inAreaTarget.getX() < 0) &&
                 !(inAreaTarget.getY() < 0) &&
                 !(inAreaTarget.getZ() < 0) &&
                 !(inAreaTarget.getX() >= 1) &&
                 !(inAreaTarget.getY() >= 1) &&
                 !(inAreaTarget.getZ() >= 1);
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
        return BlockPosStreamProvider.getForRange(BITS_PER_BLOCK_SIDE)
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
    public void setInAreaTarget(final BlockState blockState, final Vector3d inAreaTarget) throws SpaceOccupiedException
    {
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0 ||
              inAreaTarget.getX() >= 1 ||
              inAreaTarget.getY() >= 1 ||
              inAreaTarget.getZ() >= 1)
        {
            throw new IllegalArgumentException("Target is not in the current area.");
        }

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE));

        final BlockState currentState = this.compressedSection.getBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ()
        );

        if (!currentState.isAir(new SingleBlockBlockReader(
          currentState
        ), BlockPos.ZERO))
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
    public void setInBlockTarget(final BlockState blockState, final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget) throws SpaceOccupiedException
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
    public void clearInAreaTarget(final Vector3d inAreaTarget)
    {
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0 ||
              inAreaTarget.getX() >= 1 ||
              inAreaTarget.getY() >= 1 ||
              inAreaTarget.getZ() >= 1)
        {
            throw new IllegalArgumentException("Target is not in the current area.");
        }

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE));

        final BlockState blockState = Blocks.AIR.getDefaultState();

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
    public void clearInBlockTarget(final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget)
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
    public void serializeInto(@NotNull final PacketBuffer packetBuffer)
    {
        compressedSection.getData().write(packetBuffer);
    }

    /**
     * Used to read the data from the packet buffer into the current instance. Potentially overriding the data that currently already exists in the instance.
     *
     * @param packetBuffer The packet buffer to read from.
     */
    @Override
    public void deserializeFrom(@NotNull final PacketBuffer packetBuffer)
    {
        compressedSection.getData().read(packetBuffer);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = new CompoundNBT();

        final CompoundNBT chiselBlockData = new CompoundNBT();
        final CompoundNBT compressedSectionData = ChunkSectionUtils.serializeNBT(this.compressedSection);
        final CompoundNBT statisticsData = this.statistics.serializeNBT();

        chiselBlockData.put(NbtConstants.COMPRESSED_STORAGE, compressedSectionData);
        chiselBlockData.put(NbtConstants.STATISTICS, statisticsData);

        nbt.put(NbtConstants.CHISEL_BLOCK_ENTITY_DATA, chiselBlockData);

        return nbt;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        final CompoundNBT chiselBlockData = nbt.getCompound(NbtConstants.CHISEL_BLOCK_ENTITY_DATA);
        final CompoundNBT compressedSectionData = chiselBlockData.getCompound(NbtConstants.COMPRESSED_STORAGE);
        final CompoundNBT statisticsData = chiselBlockData.getCompound(NbtConstants.STATISTICS);

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
    public ItemStack toItemStack()
    {
        return sourceStack.copy();
    }

    @Override
    public Stream<IStateEntryInfo> streamWithPositionMutator(final IPositionMutator positionMutator)
    {
        return BlockPosStreamProvider.getForRange(BITS_PER_BLOCK_SIDE)
                 .map(positionMutator::mutate)
                 .map(blockPos -> new StateEntry(
                     this.compressedSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                     blockPos,
                     this::setInAreaTarget,
                     this::clearInAreaTarget
                   )
                 );
    }

    private static final class ShapeIdentifier implements IAreaShapeIdentifier
    {
        private final long[] dataArray;

        private ShapeIdentifier(final ChunkSection chunkSection)
        {
            dataArray = Arrays.copyOf(
              chunkSection.getData().storage.getBackingLongArray(),
              chunkSection.getData().storage.getBackingLongArray().length
            );
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof ShapeIdentifier))
            {
                return false;
            }
            final ShapeIdentifier that = (ShapeIdentifier) o;
            return Arrays.equals(dataArray, that.dataArray);
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode(dataArray);
        }
    }

    private static final class StateEntry implements IMutableStateEntryInfo
    {

        private final BlockState state;
        private final Vector3d   startPoint;
        private final Vector3d   endPoint;

        private final StateSetter  stateSetter;
        private final StateClearer stateClearer;

        public StateEntry(
          final BlockState state,
          final Vector3i startPoint,
          final StateSetter stateSetter,
          final StateClearer stateClearer)
        {
            this(
              state,
              Vector3d.copy(startPoint).mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT),
              Vector3d.copy(startPoint).mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT).add(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT),
              stateSetter, stateClearer);
        }

        private StateEntry(
          final BlockState state,
          final Vector3d startPoint,
          final Vector3d endPoint,
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

        private       BlockState               primaryState = Blocks.AIR.getDefaultState();
        private final Map<BlockState, Integer> countMap     = Maps.newConcurrentMap();

        @Override
        public BlockState getPrimaryState()
        {
            return primaryState;
        }

        private void clear() {
            this.primaryState = Blocks.AIR.getDefaultState();

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
            primaryState = this.countMap.entrySet().stream().min((o1, o2) -> -1 * (o1.getValue() - o2.getValue()))
                             .map(Map.Entry::getKey)
                             .orElseGet(Blocks.AIR::getDefaultState);
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT nbt = new CompoundNBT();

            nbt.put(NbtConstants.PRIMARY_STATE, NBTUtil.writeBlockState(this.primaryState));

            final ListNBT blockStateList = new ListNBT();
            for (final Map.Entry<BlockState, Integer> blockStateIntegerEntry : this.countMap.entrySet())
            {
                final CompoundNBT stateNbt = new CompoundNBT();

                stateNbt.put(NbtConstants.BLOCK_STATE, NBTUtil.writeBlockState(blockStateIntegerEntry.getKey()));
                stateNbt.putInt(NbtConstants.COUNT, blockStateIntegerEntry.getValue());

                blockStateList.add(stateNbt);
            }

            nbt.put(NbtConstants.BLOCK_STATES, blockStateList);

            return nbt;
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            this.countMap.clear();

            this.primaryState = NBTUtil.readBlockState(nbt.getCompound(NbtConstants.PRIMARY_STATE));

            final ListNBT blockStateList = nbt.getList(NbtConstants.BLOCK_STATES, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < blockStateList.size(); i++)
            {
                final CompoundNBT stateNbt = blockStateList.getCompound(i);

                this.countMap.put(
                  NBTUtil.readBlockState(stateNbt.getCompound(NbtConstants.BLOCK_STATE)),
                  stateNbt.getInt(NbtConstants.COUNT)
                );
            }
        }

        public void initializeFrom(final ChunkSection compressedSection)
        {
            this.clear();

            compressedSection.getData().count(countMap::putIfAbsent);
            updatePrimaryState();
        }
    }

}
