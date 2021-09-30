package mod.chiselsandbits.block.entities;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.identifier.ILongArrayBackedAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.callback.StateClearer;
import mod.chiselsandbits.api.multistate.mutator.callback.StateSetter;
import mod.chiselsandbits.api.multistate.mutator.world.IInWorldMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.multistate.statistics.IMultiStateObjectStatistics;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import mod.chiselsandbits.api.util.SingleBlockWorldReader;
import mod.chiselsandbits.api.util.Vector2i;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.network.packets.TileEntityUpdatedPacket;
import mod.chiselsandbits.registrars.ModTileEntityTypes;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class ChiseledBlockEntity extends BlockEntity implements IMultiStateBlockEntity
{
    public static final float ONE_THOUSANDS       = 1 / 1000f;
    private final MutableStatistics mutableStatistics;
    private final Map<UUID, IBatchMutation> batchMutations = Maps.newConcurrentMap();
    private       LevelChunkSection      compressedSection;

    public ChiseledBlockEntity(BlockPos position, BlockState state)
    {
        super(ModTileEntityTypes.CHISELED.get(), position, state);
        compressedSection = new LevelChunkSection(0); //We always use a minimal y level to lookup. Makes calculations internally easier.
        mutableStatistics = new MutableStatistics(this::getLevel, this::getBlockPos);
    }

    @Override
    public IAreaShapeIdentifier createNewShapeIdentifier()
    {
        return new Identifier(this.compressedSection);
    }

    @Override
    public Stream<IStateEntryInfo> stream()
    {
        return BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
                 .map(blockPos -> new StateEntry(
                   compressedSection.getBlockState(
                     blockPos.getX(),
                     blockPos.getY(),
                     blockPos.getZ()
                   ),
                   getLevel(),
                   getBlockPos(),
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

        return Optional.of(new StateEntry(
          currentState,
          getLevel(),
          getBlockPos(),
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

        return this.isInside(
          inBlockTarget
        );
    }    @Override
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
        return MultiStateSnapshotUtils.createFromSection(this.compressedSection);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void load( @NotNull final CompoundTag nbt)
    {
        super.load(nbt);

        this.deserializeNBT(nbt);
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

        mutableStatistics.deserializeNBT(statisticsData);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag nbt = super.serializeNBT();
        return save(nbt);
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket pkt)
    {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag)
    {
        final byte[] compressedStorageData = tag.getByteArray(NbtConstants.CHISEL_BLOCK_ENTITY_DATA);

        final ByteBuf buffer = Unpooled.wrappedBuffer(compressedStorageData);
        this.deserializeFrom(new FriendlyByteBuf(buffer));
        buffer.release();
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull final CompoundTag compound)
    {
        final CompoundTag nbt = super.save(compound);
        final CompoundTag chiselBlockData = new CompoundTag();
        final CompoundTag compressedSectionData = ChunkSectionUtils.serializeNBT(this.compressedSection);
        chiselBlockData.put(NbtConstants.COMPRESSED_STORAGE, compressedSectionData);
        chiselBlockData.put(NbtConstants.STATISTICS, mutableStatistics.serializeNBT());

        nbt.put(NbtConstants.CHISEL_BLOCK_ENTITY_DATA, chiselBlockData);

        return nbt;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it hasn't changed and skip it.
     */
    @Override
    public void setChanged()
    {
        if (getLevel() != null && this.batchMutations.isEmpty())
        {
            mutableStatistics.recalculate(this.compressedSection);

            super.setChanged();

            getLevel().getLightEngine().checkBlock(getBlockPos());
            getLevel().sendBlockUpdated(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState(), Constants.BlockFlags.DEFAULT);

            if (!getLevel().isClientSide())
            {
                ChiselsAndBits.getInstance().getNetworkChannel().sendToTrackingChunk(
                  new TileEntityUpdatedPacket(this),
                  getLevel().getChunkAt(getBlockPos())
                );
            }
        }
    }

    private boolean shouldUpdateWorld() {
        return this.getLevel() != null && this.batchMutations.size() == 0 && this.getLevel() instanceof ServerLevel;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return new ClientboundBlockEntityDataPacket(worldPosition, 255, getUpdateTag());
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        //Special compound version which just contains the bit array!
        final CompoundTag updateTag = super.getUpdateTag();

        final ByteBuf buffer = Unpooled.buffer();
        final FriendlyByteBuf innerPacketBuffer = new FriendlyByteBuf(buffer);
        this.serializeInto(innerPacketBuffer);
        final byte[] data = buffer.array();
        buffer.release();

        updateTag.putByteArray(NbtConstants.CHISEL_BLOCK_ENTITY_DATA, data);

        return updateTag;
    }

    @Override
    public void serializeInto(@NotNull final FriendlyByteBuf packetBuffer)
    {
        compressedSection.write(packetBuffer);
        mutableStatistics.serializeInto(packetBuffer);
    }

    @Override
    public void deserializeFrom(@NotNull final FriendlyByteBuf packetBuffer)
    {
        compressedSection.read(packetBuffer);
        mutableStatistics.deserializeFrom(packetBuffer);
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
                   compressedSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                   getLevel(),
                   getBlockPos(),
                   blockPos,
                   this::setInAreaTarget,
                   this::clearInAreaTarget)
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

        final BlockState currentState = this.compressedSection.getBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ()
        );

        if (!currentState.isAir())
        {
            throw new SpaceOccupiedException();
        }

        if (getLevel() == null)
        {
            return;
        }

        this.compressedSection.setBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ(),
          blockState,
          true
        );

        if (blockState.isAir() && !currentState.isAir())
        {
            mutableStatistics.onBlockStateRemoved(currentState, inAreaPos, shouldUpdateWorld());
        }
        else if (!blockState.isAir() && currentState.isAir())
        {
            mutableStatistics.onBlockStateAdded(blockState, inAreaPos, shouldUpdateWorld());
        }
        else if (!blockState.isAir() && !currentState.isAir())
        {
            mutableStatistics.onBlockStateReplaced(currentState, blockState, inAreaPos, shouldUpdateWorld());
        }

        if (getLevel() != null)
        {
            setChanged();
        }
    }

    @Override
    public LevelAccessor getWorld()
    {
        return getLevel();
    }

    @Override
    public Vec3 getInWorldStartPoint()
    {
        return Vec3.atLowerCornerOf(getBlockPos());
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
    }    @Override
    public Vec3 getInWorldEndPoint()
    {
        return getInWorldStartPoint().add(1, 1, 1).subtract(ONE_THOUSANDS, ONE_THOUSANDS, ONE_THOUSANDS);
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

        if (getLevel() == null)
        {
            return;
        }

        final BlockState currentState = this.compressedSection.getBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ()
        );

        if (!IEligibilityManager.getInstance().canBeChiseled(currentState))
        {
            return;
        }

        final BlockState blockState = Blocks.AIR.defaultBlockState();

        this.compressedSection.setBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ(),
          blockState,
          true
        );

        if (blockState.isAir() && !currentState.isAir())
        {
            mutableStatistics.onBlockStateRemoved(currentState, inAreaPos, shouldUpdateWorld());
        }
        else if (!blockState.isAir() && currentState.isAir())
        {
            mutableStatistics.onBlockStateAdded(blockState, inAreaPos, shouldUpdateWorld());
        }
        else if (!blockState.isAir() && !currentState.isAir())
        {
            mutableStatistics.onBlockStateReplaced(currentState, blockState, inAreaPos, shouldUpdateWorld());
        }

        if (getLevel() != null)
        {
            setChanged();
        }
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

    @Override
    public IMultiStateObjectStatistics getStatistics()
    {
        return mutableStatistics;
    }

    @Override
    public void rotate(final Direction.Axis axis, final int rotationCount)
    {
        if (getLevel() == null)
        {
            return;
        }

        //Large operation, better batch this together to prevent weird updates.
        try(final IBatchMutation ignored = batch()) {
            this.compressedSection = ChunkSectionUtils.rotate90Degrees(
              this.compressedSection,
              axis,
              rotationCount
            );
            this.mutableStatistics.clear();

            BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
              .forEach(position -> this.mutableStatistics.onBlockStateAdded(
                this.compressedSection.getBlockState(position.getX(), position.getY(), position.getZ()),
                position,
                shouldUpdateWorld()
              ));
        }
    }

    /**
     * Initializes the block entity so that all its state entries have the given state as their state.
     *
     * @param currentState The new initial state.
     */
    @Override
    public void initializeWith(final BlockState currentState)
    {
        if (getLevel() == null)
        {
            return;
        }

        try(IBatchMutation batchMutation = batch()) {
            BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
              .forEach(blockPos -> this.compressedSection.setBlockState(
                blockPos.getX(),
                blockPos.getY(),
                blockPos.getZ(),
                currentState
              ));

            this.mutableStatistics.initializeWith(currentState);
        }
    }

    /**
     * Returns all entries in the current area in a mutable fashion. Includes all empty areas as areas containing an air state.
     *
     * @return A stream with a mutable state entry info for each mutable section in the area.
     */
    @Override
    public Stream<IInWorldMutableStateEntryInfo> inWorldMutableStream()
    {
        return BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
                 .map(blockPos -> new StateEntry(
                   compressedSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                   getLevel(),
                   getBlockPos(),
                   blockPos,
                   this::setInAreaTarget,
                   this::clearInAreaTarget)
                 );
    }

    @Override
    public Stream<IStateEntryInfo> streamWithPositionMutator(final IPositionMutator positionMutator)
    {
        return BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
                 .map(positionMutator::mutate)
                 .map(blockPos -> new StateEntry(
                   compressedSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                   getLevel(),
                   getBlockPos(),
                   blockPos,
                   this::setInAreaTarget,
                   this::clearInAreaTarget)
                 );
    }

    @Override
    public IBatchMutation batch()
    {
        final UUID id = UUID.randomUUID();

        this.batchMutations.put(id, new BatchMutationLock(() -> {
            this.batchMutations.remove(id);

            if (this.batchMutations.isEmpty())
            {
                setChanged();
            }
        }));
        return this.batchMutations.get(id);
    }

    private static final class StateEntry implements IInWorldMutableStateEntryInfo
    {

        private final BlockState state;
        private final LevelAccessor     reader;
        private final BlockPos   blockPos;
        private final Vec3   startPoint;
        private final Vec3   endPoint;

        private final StateSetter  stateSetter;
        private final StateClearer stateClearer;

        public StateEntry(
          final BlockState state,
          final LevelAccessor reader,
          final BlockPos blockPos,
          final Vec3i startPoint,
          final StateSetter stateSetter,
          final StateClearer stateClearer)
        {
            this(
              state,
              reader,
              blockPos,
              Vec3.atLowerCornerOf(startPoint).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit()),
              Vec3.atLowerCornerOf(startPoint).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit()).add(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit()),
              stateSetter, stateClearer);
        }

        private StateEntry(
          final BlockState state,
          final LevelAccessor reader,
          final BlockPos blockPos,
          final Vec3 startPoint,
          final Vec3 endPoint,
          final StateSetter stateSetter,
          final StateClearer stateClearer)
        {
            this.state = state;
            this.reader = reader;
            this.blockPos = blockPos;
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

        @Override
        public LevelAccessor getWorld()
        {
            return reader;
        }

        @Override
        public BlockPos getBlockPos()
        {
            return blockPos;
        }
    }

    private static final class MutableStatistics implements IMultiStateObjectStatistics, INBTSerializable<CompoundTag>, IPacketBufferSerializable
    {

        private final Supplier<LevelAccessor>   worldReaderSupplier;
        private final Supplier<BlockPos> positionSupplier;
        private final Map<BlockState, Integer> countMap     = Maps.newConcurrentMap();
        private final Multimap<Vector2i, Integer> columnBlockedMap = HashMultimap.create();
        private       BlockState               primaryState = Blocks.AIR.defaultBlockState();
        private int   totalUsedBlockCount           = 0;
        private int   totalUsedChecksWeakPowerCount = 0;
        private float totalUpperSurfaceSlipperiness = 0f;
        private int   totalLightLevel               = 0;

        private MutableStatistics(final Supplier<LevelAccessor> worldReaderSupplier, final Supplier<BlockPos> positionSupplier)
        {
            this.worldReaderSupplier = worldReaderSupplier;
            this.positionSupplier = positionSupplier;
        }

        @Override
        public BlockState getPrimaryState()
        {
            return primaryState;
        }

        @Override
        public boolean isEmpty()
        {
            return this.countMap.size() == 1 && this.countMap.getOrDefault(Blocks.AIR.defaultBlockState(), 0) == 4096;
        }

        @Override
        public Map<BlockState, Integer> getStateCounts()
        {
            return Collections.unmodifiableMap(countMap);
        }

        @Override
        public boolean shouldCheckWeakPower()
        {
            return totalUsedChecksWeakPowerCount == StateEntrySize.current().getBitsPerBlock();
        }

        @Override
        public float getFullnessFactor()
        {
            return totalUsedBlockCount / (float) StateEntrySize.current().getBitsPerBlock();
        }

        @Override
        public float getSlipperiness()
        {
            return totalUpperSurfaceSlipperiness / (float) StateEntrySize.current().getBitsPerLayer();
        }

        @Override
        public float getLightEmissionFactor()
        {
            return this.totalLightLevel / (float) this.totalUsedBlockCount;
        }

        @Override
        public float getRelativeBlockHardness(final Player player)
        {
            final double totalRelativeHardness = (this.countMap.entrySet().stream()
                                                    .mapToDouble(entry -> (double) entry.getKey().getDestroyProgress(
                                                      player,
                                                      new SingleBlockWorldReader(
                                                        entry.getKey(),
                                                        this.positionSupplier.get(),
                                                        this.worldReaderSupplier.get()
                                                      ),
                                                      this.positionSupplier.get()
                                                    ) * entry.getValue())
                                                    .filter(Double::isFinite)
                                                    .sum());

            if (totalRelativeHardness == 0 || Double.isNaN(totalRelativeHardness) || Double.isInfinite(totalRelativeHardness))
            {
                return 0;
            }

            return (float) (totalRelativeHardness / totalUsedBlockCount);
        }

        @Override
        public boolean canPropagateSkylight()
        {
            for (int x = 0; x < StateEntrySize.current().getBitsPerBlockSide(); x++)
            {
                for (int y = 0; y < StateEntrySize.current().getBitsPerBlockSide(); y++)
                {
                    final Vector2i coordinate = new Vector2i(x, y);

                    if (!this.columnBlockedMap.containsKey(coordinate))
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        private void onBlockStateAdded(final BlockState blockState, final BlockPos pos, final boolean updateWorld)
        {
            countMap.putIfAbsent(blockState, 0);
            countMap.computeIfPresent(blockState, (state, currentCount) -> currentCount + 1);
            updatePrimaryState(updateWorld);

            this.totalUsedBlockCount++;

            if (blockState.shouldCheckWeakPower(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get(),
              Direction.NORTH
            ))
            {
                this.totalUsedChecksWeakPowerCount++;
            }

            if (pos.getY() == 15)
            {
                this.totalUpperSurfaceSlipperiness += blockState.getFriction(
                  new SingleBlockWorldReader(
                    blockState,
                    this.positionSupplier.get(),
                    this.worldReaderSupplier.get()
                  ),
                  this.positionSupplier.get(),
                  null
                );
            }

            this.totalLightLevel += blockState.getLightEmission(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            );

            if (!blockState.propagatesSkylightDown(new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()))
            {
                this.columnBlockedMap.put(
                  new Vector2i(pos.getX(), pos.getZ()),
                  pos.getY()
                );
            }
        }

        private void updatePrimaryState(final boolean updateWorld)
        {
            final BlockState currentPrimary = primaryState;
            primaryState = this.countMap.entrySet()
                             .stream()
                             .filter(e -> !e.getKey().isAir())
                             .min((o1, o2) -> -1 * (o1.getValue() - o2.getValue()))
                             .map(Map.Entry::getKey)
                             .orElseGet(Blocks.AIR::defaultBlockState);

            final boolean primaryIsAir = this.primaryState.isAir();

            if ((this.countMap.getOrDefault(primaryState, 0) == StateEntrySize.current().getBitsPerBlock() || primaryIsAir || currentPrimary != primaryState) && updateWorld) {
                if (primaryIsAir) {
                    this.worldReaderSupplier.get().setBlock(
                      this.positionSupplier.get(),
                      Blocks.AIR.defaultBlockState(),
                      Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.UPDATE_NEIGHBORS
                    );
                }
                else if (this.countMap.getOrDefault(primaryState, 0) == StateEntrySize.current().getBitsPerBlock())
                {
                    this.worldReaderSupplier.get().setBlock(
                      this.positionSupplier.get(),
                      this.primaryState,
                      Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.UPDATE_NEIGHBORS
                    );
                }
                else if (currentPrimary != primaryState) {
                    final Optional<Block> optionalWithConvertedBlock = IConversionManager.getInstance().getChiseledVariantOf(this.primaryState);
                    if (optionalWithConvertedBlock.isPresent())
                    {
                        final Block convertedBlock = optionalWithConvertedBlock.get();
                        this.worldReaderSupplier.get().setBlock(
                          this.positionSupplier.get(),
                          convertedBlock.defaultBlockState(),
                          Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.UPDATE_NEIGHBORS
                        );
                    }
                }
            }
        }

        private void onBlockStateRemoved(final BlockState blockState, final BlockPos pos, final boolean updateWorld)
        {
            countMap.computeIfPresent(blockState, (state, currentCount) -> currentCount - 1);
            countMap.remove(blockState, 0);
            updatePrimaryState(updateWorld);

            this.totalUsedBlockCount--;

            if (blockState.shouldCheckWeakPower(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get(),
              Direction.NORTH
            ))
            {
                this.totalUsedChecksWeakPowerCount--;
            }

            if (pos.getY() == 15)
            {
                this.totalUpperSurfaceSlipperiness -= blockState.getFriction(
                  new SingleBlockWorldReader(
                    blockState,
                    this.positionSupplier.get(),
                    this.worldReaderSupplier.get()
                  ),
                  this.positionSupplier.get(),
                  null
                );
            }

            this.totalLightLevel -= blockState.getLightEmission(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            );

            this.columnBlockedMap.remove(
              new Vector2i(pos.getX(), pos.getZ()),
              pos.getY()
            );
        }

        private void onBlockStateReplaced(final BlockState currentState, final BlockState newState, final BlockPos pos, final boolean updateWorld)
        {
            countMap.computeIfPresent(currentState, (state, currentCount) -> currentCount - 1);
            countMap.remove(currentState, 0);
            countMap.putIfAbsent(newState, 0);
            countMap.computeIfPresent(newState, (state, currentCount) -> currentCount + 1);
            updatePrimaryState(updateWorld);

            if (currentState.shouldCheckWeakPower(
              new SingleBlockWorldReader(
                currentState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get(),
              Direction.NORTH
            ))
            {
                this.totalUsedChecksWeakPowerCount--;
            }

            if (newState.shouldCheckWeakPower(
              new SingleBlockWorldReader(
                newState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get(),
              Direction.NORTH
            ))
            {
                this.totalUsedChecksWeakPowerCount++;
            }

            if (pos.getY() == 15)
            {
                this.totalUpperSurfaceSlipperiness -= currentState.getFriction(
                  new SingleBlockWorldReader(
                    currentState,
                    this.positionSupplier.get(),
                    this.worldReaderSupplier.get()
                  ),
                  this.positionSupplier.get(),
                  null
                );

                this.totalUpperSurfaceSlipperiness += newState.getFriction(
                  new SingleBlockWorldReader(
                    newState,
                    this.positionSupplier.get(),
                    this.worldReaderSupplier.get()
                  ),
                  this.positionSupplier.get(),
                  null
                );
            }

            this.totalLightLevel -= currentState.getLightEmission(
              new SingleBlockWorldReader(
                currentState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            );

            this.totalLightLevel += newState.getLightEmission(
              new SingleBlockWorldReader(
                newState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            );

            if (currentState.propagatesSkylightDown(
              new SingleBlockWorldReader(
                newState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            ))
            {
                if (!newState.propagatesSkylightDown(
                  new SingleBlockWorldReader(
                    newState,
                    this.positionSupplier.get(),
                    this.worldReaderSupplier.get()
                  ),
                  this.positionSupplier.get()
                ))
                {
                    this.columnBlockedMap.remove(new Vector2i(pos.getX(), pos.getZ()), pos.getY());
                }
            }
            else if (newState.propagatesSkylightDown(
              new SingleBlockWorldReader(
                newState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            ))
            {
                this.columnBlockedMap.put(new Vector2i(pos.getX(), pos.getZ()), pos.getY());
            }
        }

        @Override
        public void serializeInto(@NotNull final FriendlyByteBuf packetBuffer)
        {
            packetBuffer.writeVarInt(IBlockStateIdManager.getInstance().getIdFrom(this.primaryState));

            packetBuffer.writeVarInt(this.countMap.size());
            for (final Map.Entry<BlockState, Integer> blockStateIntegerEntry : this.countMap.entrySet())
            {
                packetBuffer.writeVarInt(IBlockStateIdManager.getInstance().getIdFrom(blockStateIntegerEntry.getKey()));
                packetBuffer.writeVarInt(blockStateIntegerEntry.getValue());
            }
            packetBuffer.writeVarInt(this.columnBlockedMap.size());
            for (final Map.Entry<Vector2i, Integer> vector2iIntegerEntry : this.columnBlockedMap.entries())
            {
                packetBuffer.writeVarInt(vector2iIntegerEntry.getKey().getX());
                packetBuffer.writeVarInt(vector2iIntegerEntry.getKey().getY());
                packetBuffer.writeVarInt(vector2iIntegerEntry.getValue());
            }

            packetBuffer.writeVarInt(this.totalUsedBlockCount);
            packetBuffer.writeVarInt(this.totalUsedChecksWeakPowerCount);
            packetBuffer.writeFloat(this.totalUpperSurfaceSlipperiness);
            packetBuffer.writeVarInt(this.totalLightLevel);
        }

        @Override
        public void deserializeFrom(@NotNull final FriendlyByteBuf packetBuffer)
        {
            this.countMap.clear();
            this.columnBlockedMap.clear();

            this.primaryState = IBlockStateIdManager.getInstance().getBlockStateFrom(packetBuffer.readVarInt());

            final int stateCount = packetBuffer.readVarInt();
            for (int i = 0; i < stateCount; i++)
            {
                this.countMap.put(
                  IBlockStateIdManager.getInstance().getBlockStateFrom(packetBuffer.readVarInt()),
                  packetBuffer.readVarInt()
                );
            }
            final int columnBlockCount = packetBuffer.readVarInt();
            for (int i = 0; i < columnBlockCount; i++)
            {
                this.columnBlockedMap.put(
                  new Vector2i(
                    packetBuffer.readVarInt(),
                    packetBuffer.readVarInt()
                  ),
                  packetBuffer.readVarInt()
                );
            }

            this.totalUsedBlockCount = packetBuffer.readVarInt();
            this.totalUsedChecksWeakPowerCount = packetBuffer.readVarInt();
            this.totalUpperSurfaceSlipperiness = packetBuffer.readFloat();
            this.totalLightLevel = packetBuffer.readVarInt();
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
            final ListTag columnBlockList = new ListTag();
            for (final Map.Entry<Vector2i, Integer> vector2iIntegerEntry : this.columnBlockedMap.entries())
            {
                final CompoundTag columnBlockNbt = new CompoundTag();
                final CompoundTag coordinateNbt = new CompoundTag();

                coordinateNbt.putInt(NbtConstants.X_COORDINATE, vector2iIntegerEntry.getKey().getX());
                coordinateNbt.putInt(NbtConstants.Y_COORDINATE, vector2iIntegerEntry.getKey().getY());

                columnBlockNbt.put(NbtConstants.COORDINATE, coordinateNbt);
                columnBlockNbt.putInt(NbtConstants.VALUE, vector2iIntegerEntry.getValue());

                columnBlockList.add(columnBlockNbt);
            }

            nbt.put(NbtConstants.BLOCK_STATES, blockStateList);
            nbt.put(NbtConstants.COLUMN_BLOCK_LIST, columnBlockList);

            nbt.putInt(NbtConstants.TOTAL_BLOCK_COUNT, totalUsedBlockCount);
            nbt.putInt(NbtConstants.TOTAL_SHOULD_CHECK_WEAK_POWER_COUNT, totalUsedChecksWeakPowerCount);
            nbt.putFloat(NbtConstants.TOTAL_UPPER_LEVEL_SLIPPERINESS, totalUpperSurfaceSlipperiness);
            nbt.putInt(NbtConstants.TOTAL_LIGHT_LEVEL, totalLightLevel);

            return nbt;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.countMap.clear();

            this.primaryState = NbtUtils.readBlockState(nbt.getCompound(NbtConstants.PRIMARY_STATE));

            final ListTag blockStateList = nbt.getList(NbtConstants.BLOCK_STATES, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < blockStateList.size(); i++)
            {
                final CompoundTag stateNbt = blockStateList.getCompound(i);

                this.countMap.put(
                  NbtUtils.readBlockState(stateNbt.getCompound(NbtConstants.BLOCK_STATE)),
                  stateNbt.getInt(NbtConstants.COUNT)
                );
            }

            final ListTag columnBlockList = nbt.getList(NbtConstants.COLUMN_BLOCK_LIST, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < columnBlockList.size(); i++)
            {
                final CompoundTag columnBlockNbt = columnBlockList.getCompound(i);
                final CompoundTag coordinateNbt = columnBlockNbt.getCompound(NbtConstants.COORDINATE);

                this.columnBlockedMap.put(
                  new Vector2i(
                    coordinateNbt.getInt(NbtConstants.X_COORDINATE),
                    coordinateNbt.getInt(NbtConstants.Y_COORDINATE)
                  ),
                  columnBlockNbt.getInt(NbtConstants.VALUE)
                );
            }

            this.totalUsedBlockCount = nbt.getInt(NbtConstants.TOTAL_BLOCK_COUNT);
            this.totalUsedChecksWeakPowerCount = nbt.getInt(NbtConstants.TOTAL_SHOULD_CHECK_WEAK_POWER_COUNT);
            this.totalUpperSurfaceSlipperiness = nbt.getFloat(NbtConstants.TOTAL_UPPER_LEVEL_SLIPPERINESS);
            this.totalLightLevel = nbt.getInt(NbtConstants.TOTAL_LIGHT_LEVEL);
        }

        public void initializeWith(final BlockState blockState)
        {
            clear();
            final boolean isAir = blockState.isAir();

            this.primaryState = blockState;
            if (!isAir)
            {
                this.countMap.put(blockState, StateEntrySize.current().getBitsPerBlock());
            }
            this.totalUsedBlockCount = isAir ? 0 : StateEntrySize.current().getBitsPerBlock();

            if (blockState.shouldCheckWeakPower(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get(),
              Direction.NORTH
            ))
            {
                this.totalUsedChecksWeakPowerCount = StateEntrySize.current().getBitsPerBlock();
            }

            this.totalLightLevel += (blockState.getLightEmission(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            ) * StateEntrySize.current().getBitsPerBlock());

            this.totalUpperSurfaceSlipperiness += (blockState.getFriction(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get(),
              null
            ) * StateEntrySize.current().getBitsPerBlock());


            if (!blockState.propagatesSkylightDown(new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()))
            {
                BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
                  .forEach(pos -> columnBlockedMap.put(new Vector2i(pos.getX(), pos.getZ()), pos.getY()));
            }
        }

        private void clear()
        {
            this.primaryState = Blocks.AIR.defaultBlockState();

            this.countMap.clear();
            this.columnBlockedMap.clear();

            this.totalUsedBlockCount = 0;
            this.totalUsedChecksWeakPowerCount = 0;
            this.totalUpperSurfaceSlipperiness = 0;
            this.totalLightLevel = 0;
        }

        private void recalculate(final LevelChunkSection source)
        {
            clear();

            source.getStates().count(countMap::put);
            countMap.remove(Blocks.AIR.defaultBlockState());
            updatePrimaryState(true);

            this.totalUsedBlockCount = countMap.values().stream().mapToInt(i -> i).sum();

            countMap.forEach((blockState, count) -> {
                if (blockState.shouldCheckWeakPower(
                  new SingleBlockWorldReader(
                    blockState,
                    this.positionSupplier.get(),
                    this.worldReaderSupplier.get()
                  ),
                  this.positionSupplier.get(),
                  Direction.NORTH
                ))
                {
                    this.totalUsedChecksWeakPowerCount += count;
                }

                this.totalLightLevel += (blockState.getLightEmission(
                  new SingleBlockWorldReader(
                    blockState,
                    this.positionSupplier.get(),
                    this.worldReaderSupplier.get()
                  ),
                  this.positionSupplier.get()
                ) * count);
            });

            BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
              .forEach(pos -> {
                  final BlockState blockState = source.getBlockState(pos.getX(), pos.getY(), pos.getZ());
                  if (pos.getY() == 15)
                  {
                      this.totalUpperSurfaceSlipperiness += (blockState.getFriction(
                        new SingleBlockWorldReader(
                          blockState,
                          this.positionSupplier.get(),
                          this.worldReaderSupplier.get()
                        ),
                        this.positionSupplier.get(),
                        null
                      ));
                  }

                  if (!blockState.propagatesSkylightDown(
                    new SingleBlockWorldReader(
                      blockState,
                      this.positionSupplier.get(),
                      this.worldReaderSupplier.get()
                    ),
                    this.positionSupplier.get()
                  ))
                  {
                      columnBlockedMap.put(new Vector2i(pos.getX(), pos.getZ()), pos.getY());
                  }
              });
        }
    }

    private static final class Identifier implements ILongArrayBackedAreaShapeIdentifier
    {

        private final long[] identifyingPayload;

        private Identifier(final LevelChunkSection section)
        {
            this.identifyingPayload = Arrays.copyOf(
              section.getStates().storage.getRaw(),
              section.getStates().storage.getRaw().length
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
            if (!(o instanceof ILongArrayBackedAreaShapeIdentifier))
            {
                return false;
            }
            final ILongArrayBackedAreaShapeIdentifier that = (ILongArrayBackedAreaShapeIdentifier) o;
            return Arrays.equals(identifyingPayload, that.getBackingData());
        }

        @Override
        public String toString()
        {
            return "Identifier{" +
                     "identifyingPayload=" + Arrays.toString(identifyingPayload) +
                     '}';
        }

        @Override
        public long[] getBackingData()
        {
            return identifyingPayload;
        }
    }

    private record BatchMutationLock(Runnable closeCallback) implements IBatchMutation
    {

        @Override
        public void close()
        {
            this.closeCallback.run();
        }
    }








}
