package mod.chiselsandbits.block.entities;

import com.google.common.collect.*;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.block.entity.INetworkUpdateableEntity;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.identifier.IByteArrayBackedAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.callback.StateClearer;
import mod.chiselsandbits.api.multistate.mutator.callback.StateSetter;
import mod.chiselsandbits.api.multistate.mutator.world.IInWorldMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.multistate.statistics.IMultiStateObjectStatistics;
import mod.chiselsandbits.api.util.*;
import mod.chiselsandbits.block.entities.storage.SimpleStateEntryStorage;
import mod.chiselsandbits.client.model.data.ChiseledBlockModelDataManager;
import mod.chiselsandbits.network.packets.TileEntityUpdatedPacket;
import mod.chiselsandbits.platforms.core.blockstate.ILevelBasedPropertyAccessor;
import mod.chiselsandbits.platforms.core.client.models.data.IBlockModelData;
import mod.chiselsandbits.platforms.core.client.models.data.IModelDataBuilder;
import mod.chiselsandbits.platforms.core.entity.block.IBlockEntityWithModelData;
import mod.chiselsandbits.platforms.core.util.constants.NbtConstants;
import mod.chiselsandbits.registrars.ModBlockEntityTypes;
import mod.chiselsandbits.storage.ILegacyStorageHandler;
import mod.chiselsandbits.storage.IMultiThreadedStorageEngine;
import mod.chiselsandbits.storage.IStorageHandler;
import mod.chiselsandbits.storage.StorageEngineBuilder;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.chiselsandbits.utils.GZIPDataCompressionUtils;
import mod.chiselsandbits.utils.LZ4DataCompressionUtils;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class ChiseledBlockEntity extends BlockEntity implements IMultiStateBlockEntity, INetworkUpdateableEntity, IBlockEntityWithModelData
{
    public static final float ONE_THOUSANDS       = 1 / 1000f;

    private final MutableStatistics mutableStatistics;
    private final Map<UUID, IBatchMutation> batchMutations = Maps.newConcurrentMap();
    private final IStateEntryStorage        compressedSection;
    private final IMultiThreadedStorageEngine storageEngine;

    private       IBlockModelData             modelData = IModelDataBuilder.create().build();

    private final Object tagSyncHandle = new Object();
    private CompoundTag lastTag = null;
    private CompletableFuture<Void> storageFuture = null;

    public ChiseledBlockEntity(BlockPos position, BlockState state)
    {
        super(ModBlockEntityTypes.CHISELED.get(), position, state);
        compressedSection = new SimpleStateEntryStorage();
        mutableStatistics = new MutableStatistics(this::getLevel, this::getBlockPos);

        storageEngine = StorageEngineBuilder.create()
                          .withLegacy(new LegacyChunkSectionBasedStorageHandler())
                          .withLegacy(new LegacyGZIPStorageBasedStorageHandler())
                          .with(new LZ4StorageBasedStorageHandler())
                          .buildMultiThreaded();
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
    }

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
        return MultiStateSnapshotUtils.createFromStorage(this.compressedSection);
    }

    @Override
    public void load( @NotNull final CompoundTag nbt)
    {
        this.deserializeNBT(nbt);
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.storageEngine.deserializeNBT(nbt);
        ChiseledBlockModelDataManager.getInstance().updateModelData(this);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return saveWithFullMetadata();
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag compound)
    {
        super.saveAdditional(compound);

        synchronized (this.tagSyncHandle) {
            if (this.lastTag != null) {
                //Offthread completed.)
                final CompoundTag nbt = this.lastTag.copy();
                nbt.getAllKeys().forEach(key -> compound.put(key, nbt.get(key)));
                return;
            }
            else if (this.storageFuture != null) {
                this.storageFuture.join();

                //Now the tag needs to be there!
                Validate.notNull(this.lastTag, "The storage future did not complete.");
                final CompoundTag nbt = this.lastTag.copy();
                nbt.getAllKeys().forEach(key -> compound.put(key, nbt.get(key)));
                return;
            }
        }

        this.storageEngine.serializeNBTInto(compound);
    }
    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it hasn't changed and skip it.
     */
    @Override
    public void setChanged()
    {
        if (getLevel() != null && this.batchMutations.isEmpty())
        {
            this.mutableStatistics.updatePrimaryState(true);

            if (!getLevel().isClientSide()) {
                synchronized (this.tagSyncHandle) {
                    if (this.storageFuture != null)
                    {
                        this.storageFuture.cancel(true);
                    }
                    this.lastTag = null;

                    this.storageFuture = this.storageEngine.serializeOffThread()
                                           .thenAcceptAsync(calculatedTag -> this.lastTag = calculatedTag);
                }
            }

            super.setChanged();

            getLevel().getLightEngine().checkBlock(getBlockPos());
            getLevel().sendBlockUpdated(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState(), Block.UPDATE_ALL);

            if (!getLevel().isClientSide())
            {
                ChiselsAndBits.getInstance().getNetworkChannel().sendToTrackingChunk(
                  new TileEntityUpdatedPacket(this),
                  getLevel().getChunkAt(getBlockPos())
                );
                getLevel().updateNeighborsAt(getBlockPos(), getLevel().getBlockState(getBlockPos()).getBlock());
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
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return this.storageEngine.serializeNBT();
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag)
    {
        this.load(tag);
    }

    @Override
    public void serializeInto(@NotNull final FriendlyByteBuf packetBuffer)
    {
        compressedSection.serializeInto(packetBuffer);
        mutableStatistics.serializeInto(packetBuffer);
    }

    @Override
    public void deserializeFrom(@NotNull final FriendlyByteBuf packetBuffer)
    {
        compressedSection.deserializeFrom(packetBuffer);
        mutableStatistics.deserializeFrom(packetBuffer);
        ChiseledBlockModelDataManager.getInstance().updateModelData(this);
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
          blockState
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
    }

    @Override
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
          blockState
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
            this.compressedSection.rotate(axis, rotationCount);
            this.mutableStatistics.recalculate(this.compressedSection);
        }
    }

    @Override
    public void mirror(final Direction.Axis axis)
    {
        if (getLevel() == null)
        {
            return;
        }

        //Large operation, better batch this together to prevent weird updates.
        try(final IBatchMutation ignored = batch()) {
            this.compressedSection.mirror(axis);
            this.mutableStatistics.clear();
            this.mutableStatistics.recalculate(this.compressedSection);
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
            this.compressedSection.initializeWith(currentState);
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
                 .map(blockPos -> {
                     final Vec3i pos = positionMutator.mutate(blockPos);
                       return new StateEntry(
                         compressedSection.getBlockState(pos.getX(), pos.getY(), pos.getZ()),
                         getLevel(),
                         getBlockPos(),
                         pos,
                         this::setInAreaTarget,
                         this::clearInAreaTarget);
                   }
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

    @Override
    public IBatchMutation batch(final IChangeTracker changeTracker)
    {
        final IBatchMutation innerMutation = batch();
        final IMultiStateSnapshot before = this.createSnapshot();
        return () -> {
            final IMultiStateSnapshot after = this.createSnapshot();
            innerMutation.close();
            changeTracker.onBlockUpdated(getBlockPos(), before, after);
        };
    }

    public void setModelData(final IBlockModelData modelData)
    {
        this.modelData = modelData;
    }

    @NotNull
    public IBlockModelData getBlockModelData()
    {
        return this.modelData;
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

    private final class MutableStatistics implements IMultiStateObjectStatistics, INBTSerializable<CompoundTag>, IPacketBufferSerializable
    {

        private final Supplier<LevelAccessor>   worldReaderSupplier;
        private final Supplier<BlockPos> positionSupplier;
        private final Map<BlockState, Integer> countMap     = Maps.newConcurrentMap();
        private final Table<Integer, Integer, ColumnStatistics> columnStatisticsTable = HashBasedTable.create();
        private       BlockState               primaryState = Blocks.AIR.defaultBlockState();
        private int   totalUsedBlockCount           = 0;
        private int   totalUsedChecksWeakPowerCount = 0;
        private int   totalLightLevel      = 0;
        private int totalLightBlockLevel = 0;

        private boolean requiresRecalculation = false;

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
            return totalUsedChecksWeakPowerCount == totalUsedBlockCount;
        }

        @Override
        public float getFullnessFactor()
        {
            return totalUsedBlockCount / (float) StateEntrySize.current().getBitsPerBlock();
        }

        @Override
        public float getSlipperiness()
        {
            return (float) this.columnStatisticsTable.values()
              .stream()
              .filter(columnStatistics -> columnStatistics.getHighestBit() >= 0)
              .mapToDouble(ColumnStatistics::getHighestBitFriction).average().orElse(0.0);
        }

        @Override
        public float getLightEmissionFactor()
        {
            return this.totalLightLevel / (float) this.totalUsedBlockCount;
        }

        @Override
        public float getLightBlockingFactor()
        {
            return this.totalLightBlockLevel / (float) StateEntrySize.current().getBitsPerBlock();
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
            return columnStatisticsTable.values()
                     .stream().allMatch(ColumnStatistics::canPropagateSkylightDown);
        }

        @Override
        public boolean canSustainGrassBelow()
        {
            return columnStatisticsTable.values()
              .stream().anyMatch(ColumnStatistics::canLowestBitSustainGrass);
        }

        private void onBlockStateAdded(final BlockState blockState, final BlockPos pos, final boolean updateWorld)
        {
            countMap.putIfAbsent(blockState, 0);
            countMap.computeIfPresent(blockState, (state, currentCount) -> currentCount + 1);
            updatePrimaryState(updateWorld);

            this.totalUsedBlockCount++;

            if (ILevelBasedPropertyAccessor.getInstance().shouldCheckWeakPower(
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

            this.totalLightLevel += ILevelBasedPropertyAccessor.getInstance().getLightEmission(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            );

            this.totalLightBlockLevel += ILevelBasedPropertyAccessor.getInstance().getLightBlock(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            );

            if (!this.columnStatisticsTable.contains(pos.getX(), pos.getZ())) {
                this.columnStatisticsTable.put(pos.getX(), pos.getZ(), new ColumnStatistics(this.worldReaderSupplier, this.positionSupplier));
            }

            this.columnStatisticsTable.get(pos.getX(), pos.getZ()).onBlockStateAdded(blockState, pos);
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
                      Block.UPDATE_ALL
                    );
                }
                else if (this.countMap.getOrDefault(primaryState, 0) == StateEntrySize.current().getBitsPerBlock())
                {
                    this.worldReaderSupplier.get().setBlock(
                      this.positionSupplier.get(),
                      this.primaryState,
                      Block.UPDATE_ALL
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
                          Block.UPDATE_ALL
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

            if (ILevelBasedPropertyAccessor.getInstance().shouldCheckWeakPower(
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

            this.totalLightLevel -= ILevelBasedPropertyAccessor.getInstance().getLightEmission(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            );

            this.totalLightBlockLevel -= ILevelBasedPropertyAccessor.getInstance().getLightBlock(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            );

            if (!this.columnStatisticsTable.contains(pos.getX(), pos.getZ())) {
                this.columnStatisticsTable.put(pos.getX(), pos.getZ(), new ColumnStatistics(this.worldReaderSupplier, this.positionSupplier));
            }

            this.columnStatisticsTable.get(pos.getX(), pos.getZ()).onBlockStateAdded(blockState, pos);
        }

        private void onBlockStateReplaced(final BlockState currentState, final BlockState newState, final BlockPos pos, final boolean updateWorld)
        {
            countMap.computeIfPresent(currentState, (state, currentCount) -> currentCount - 1);
            countMap.remove(currentState, 0);
            countMap.putIfAbsent(newState, 0);
            countMap.computeIfPresent(newState, (state, currentCount) -> currentCount + 1);
            updatePrimaryState(updateWorld);

            if (ILevelBasedPropertyAccessor.getInstance().shouldCheckWeakPower(
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

            if (ILevelBasedPropertyAccessor.getInstance().shouldCheckWeakPower(
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

            this.totalLightLevel -= ILevelBasedPropertyAccessor.getInstance().getLightEmission(
              new SingleBlockWorldReader(
                currentState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            );

            this.totalLightLevel += ILevelBasedPropertyAccessor.getInstance().getLightEmission(
              new SingleBlockWorldReader(
                newState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            );

            this.totalLightBlockLevel -= ILevelBasedPropertyAccessor.getInstance().getLightBlock(
              new SingleBlockWorldReader(
                currentState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            );

            this.totalLightBlockLevel += ILevelBasedPropertyAccessor.getInstance().getLightBlock(
              new SingleBlockWorldReader(
                newState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            );

            if (!this.columnStatisticsTable.contains(pos.getX(), pos.getZ())) {
                this.columnStatisticsTable.put(pos.getX(), pos.getZ(), new ColumnStatistics(this.worldReaderSupplier, this.positionSupplier));
            }

            this.columnStatisticsTable.get(pos.getX(), pos.getZ()).onBlockStateReplaced(currentState, newState, pos);
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

            packetBuffer.writeVarInt(this.columnStatisticsTable.size());
            this.columnStatisticsTable.cellSet()
                                        .forEach(cell -> {
                                            packetBuffer.writeVarInt(cell.getRowKey());
                                            packetBuffer.writeVarInt(cell.getColumnKey());
                                            cell.getValue().serializeInto(packetBuffer);
                                        });

            packetBuffer.writeVarInt(this.totalUsedBlockCount);
            packetBuffer.writeVarInt(this.totalUsedChecksWeakPowerCount);
            packetBuffer.writeVarInt(this.totalLightLevel);
            packetBuffer.writeVarInt(this.totalLightBlockLevel);
        }

        @Override
        public void deserializeFrom(@NotNull final FriendlyByteBuf packetBuffer)
        {
            this.countMap.clear();
            this.columnStatisticsTable.clear();

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
                final ColumnStatistics statistics = new ColumnStatistics(this.worldReaderSupplier, this.positionSupplier);

                this.columnStatisticsTable.put(
                  packetBuffer.readVarInt(),
                  packetBuffer.readVarInt(),
                  statistics
                );

                statistics.deserializeFrom(packetBuffer);
            }

            this.totalUsedBlockCount = packetBuffer.readVarInt();
            this.totalUsedChecksWeakPowerCount = packetBuffer.readVarInt();
            this.totalLightLevel = packetBuffer.readVarInt();
            this.totalLightBlockLevel = packetBuffer.readVarInt();
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

            final CompoundTag columnStatisticsTableNbt = new CompoundTag();
            this.columnStatisticsTable.rowMap().forEach((rowKey, columnStatisticsMap) -> {
               final CompoundTag rowNbt = new CompoundTag();
               columnStatisticsMap.forEach((columnKey, columnStatistics) -> rowNbt.put(String.valueOf(columnKey), columnStatistics.serializeNBT()));

               columnStatisticsTableNbt.put(String.valueOf(rowKey), rowNbt);
            });

            nbt.put(NbtConstants.BLOCK_STATES, blockStateList);
            nbt.put(NbtConstants.COLUMN_STATISTICS, columnStatisticsTableNbt);

            nbt.putInt(NbtConstants.TOTAL_BLOCK_COUNT, totalUsedBlockCount);
            nbt.putInt(NbtConstants.TOTAL_SHOULD_CHECK_WEAK_POWER_COUNT, totalUsedChecksWeakPowerCount);
            nbt.putInt(NbtConstants.TOTAL_LIGHT_LEVEL, totalLightLevel);
            nbt.putInt(NbtConstants.TOTAL_LIGHT_BLOCK_LEVEL, totalLightBlockLevel);

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

            this.columnStatisticsTable.clear();
            if (nbt.contains(NbtConstants.COLUMN_STATISTICS, Tag.TAG_COMPOUND)) {
                final CompoundTag columnStatisticsTableNbt = nbt.getCompound(NbtConstants.COLUMN_STATISTICS);
                columnStatisticsTableNbt.getAllKeys().forEach(rowKeyValue -> {
                   final Integer rowKey = Integer.valueOf(rowKeyValue);
                   final CompoundTag rowNbt = columnStatisticsTableNbt.getCompound(rowKeyValue);
                   rowNbt.getAllKeys().forEach(columnKeyValue -> {
                      final Integer columnKey = Integer.valueOf(columnKeyValue);
                      final CompoundTag columnStatisticsNbt = rowNbt.getCompound(columnKeyValue);
                      final ColumnStatistics columnStatistics = new ColumnStatistics(
                        this.worldReaderSupplier,
                        this.positionSupplier
                      );

                      columnStatistics.deserializeNBT(columnStatisticsNbt);
                   });
                });
            }
            else
            {
                requiresRecalculation = true;
            }

            this.totalUsedBlockCount = nbt.getInt(NbtConstants.TOTAL_BLOCK_COUNT);
            this.totalUsedChecksWeakPowerCount = nbt.getInt(NbtConstants.TOTAL_SHOULD_CHECK_WEAK_POWER_COUNT);
            this.totalLightLevel = nbt.getInt(NbtConstants.TOTAL_LIGHT_LEVEL);

            //We need to check if this exists or not.
            //This was added in 1.x.60+ to accommodate for the new light level system.
            if (nbt.contains(NbtConstants.TOTAL_LIGHT_BLOCK_LEVEL))
            {
                this.totalLightBlockLevel = nbt.getInt(NbtConstants.TOTAL_LIGHT_BLOCK_LEVEL);
            }
            else
            {
                this.totalLightBlockLevel = 0;
                this.requiresRecalculation = true;
            }
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

            if (ILevelBasedPropertyAccessor.getInstance().shouldCheckWeakPower(
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

            this.totalLightLevel += (ILevelBasedPropertyAccessor.getInstance().getLightEmission(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            ) * StateEntrySize.current().getBitsPerBlock());

            this.totalLightBlockLevel += (ILevelBasedPropertyAccessor.getInstance().getLightBlock(
              new SingleBlockWorldReader(
                blockState,
                this.positionSupplier.get(),
                this.worldReaderSupplier.get()
              ),
              this.positionSupplier.get()
            ) * StateEntrySize.current().getBitsPerBlock());

            this.columnStatisticsTable.clear();
            IntStream.range(0, StateEntrySize.current().getBitsPerBlockSide())
              .forEach(x -> {
                  IntStream.range(0, StateEntrySize.current().getBitsPerBlockSide())
                    .forEach(z -> {
                        final ColumnStatistics columnStatistics = new ColumnStatistics(
                          this.worldReaderSupplier,
                          this.positionSupplier
                        );
                        columnStatistics.initializeWith(blockState);
                        this.columnStatisticsTable.put(x, z, columnStatistics);
                    });
              });
        }

        private void clear()
        {
            this.primaryState = Blocks.AIR.defaultBlockState();

            this.countMap.clear();
            this.columnStatisticsTable.clear();

            this.totalUsedBlockCount = 0;
            this.totalUsedChecksWeakPowerCount = 0;
            this.totalLightLevel = 0;
            this.totalLightBlockLevel = 0;
        }

        public boolean isRequiresRecalculation()
        {
            return requiresRecalculation;
        }

        private void recalculate(final IStateEntryStorage source) {
            recalculate(source, true);
        }

        private void recalculate(final IStateEntryStorage source, final boolean mayUpdateWorld)
        {
            this.requiresRecalculation = false;
            clear();

            source.count(countMap::put);
            countMap.remove(Blocks.AIR.defaultBlockState());
            updatePrimaryState(mayUpdateWorld);

            this.totalUsedBlockCount = countMap.values().stream().mapToInt(i -> i).sum();

            countMap.forEach((blockState, count) -> {
                if (ILevelBasedPropertyAccessor.getInstance().shouldCheckWeakPower(
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

                this.totalLightLevel += (ILevelBasedPropertyAccessor.getInstance().getLightEmission(
                  new SingleBlockWorldReader(
                    blockState,
                    this.positionSupplier.get(),
                    this.worldReaderSupplier.get()
                  ),
                  this.positionSupplier.get()
                ) * count);

                this.totalLightBlockLevel += (ILevelBasedPropertyAccessor.getInstance().getLightBlock(
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

                  if (!this.columnStatisticsTable.contains(pos.getX(), pos.getZ())) {
                      this.columnStatisticsTable.put(pos.getX(), pos.getZ(), new ColumnStatistics(this.worldReaderSupplier, this.positionSupplier));
                  }

                  this.columnStatisticsTable.get(pos.getX(), pos.getZ()).onBlockStateAdded(blockState, pos);
              });
        }
    }

    private final class ColumnStatistics implements INBTSerializable<CompoundTag>, IPacketBufferSerializable
    {
        private final BitSet skylightBlockingBits = new BitSet(StateEntrySize.current().getBitsPerBlockSide());
        private final BitSet noneAirBits = new BitSet(StateEntrySize.current().getBitsPerBlockSide());
        private final Supplier<LevelAccessor>   worldReaderSupplier;
        private final Supplier<BlockPos> positionSupplier;

        private short highestBit         = -1;
        private float highestBitFriction = 0f;
        private boolean canPropagateSkylightDown = true;
        private boolean canLowestBitSustainGrass = true;

        private ColumnStatistics(final Supplier<LevelAccessor> worldReaderSupplier, final Supplier<BlockPos> positionSupplier) {
            this.worldReaderSupplier = worldReaderSupplier;
            this.positionSupplier = positionSupplier;
        }

        public BitSet getSkylightBlockingBits()
        {
            return skylightBlockingBits;
        }

        public BitSet getNoneAirBits()
        {
            return noneAirBits;
        }

        public short getHighestBit()
        {
            return highestBit;
        }

        public float getHighestBitFriction()
        {
            return highestBitFriction;
        }

        public boolean canPropagateSkylightDown()
        {
            return canPropagateSkylightDown;
        }

        public boolean canLowestBitSustainGrass()
        {
            return canLowestBitSustainGrass;
        }

        private void onBlockStateAdded(final BlockState blockState, final BlockPos pos) {
            skylightBlockingBits.set(pos.getY(), !ILevelBasedPropertyAccessor.getInstance().propagatesSkylightDown(
              new SingleBlockBlockReader(blockState, positionSupplier.get(), this.worldReaderSupplier.get()),
              positionSupplier.get()
            ));

            if (skylightBlockingBits.get(pos.getY()))
            {
                canPropagateSkylightDown = false;
            }

            if (!blockState.isAir() && pos.getY() >= highestBit)
            {
                highestBit = (short) pos.getY();
                highestBitFriction = ILevelBasedPropertyAccessor.getInstance().getFriction(
                  new SingleBlockWorldReader(blockState, positionSupplier.get(), this.worldReaderSupplier.get()),
                  positionSupplier.get(),
                  null
                );
            }

            if (pos.getY() == 0) {
                canLowestBitSustainGrass = ILevelBasedPropertyAccessor.getInstance()
                                             .canBeGrass(
                                               new SingleBlockWorldReader(blockState, positionSupplier.get(), this.worldReaderSupplier.get()),
                                               Blocks.GRASS_BLOCK.defaultBlockState(),
                                               positionSupplier.get().below(),
                                               blockState,
                                               positionSupplier.get()
                                             )
                                             .orElseGet(() -> {
                                                 if (blockState.is(Blocks.SNOW) && blockState.getValue(SnowLayerBlock.LAYERS) == 1) {
                                                     return true;
                                                 } else if (blockState.getFluidState().getAmount() == 8) {
                                                     return false;
                                                 } else {
                                                     int i = LayerLightEngine.getLightBlockInto(
                                                       new SingleBlockWorldReader(blockState, positionSupplier.get(), this.worldReaderSupplier.get()),
                                                       Blocks.GRASS_BLOCK.defaultBlockState(),
                                                       this.positionSupplier.get().below(),
                                                       blockState,
                                                       this.positionSupplier.get(),
                                                       Direction.UP,
                                                       blockState.getLightBlock(
                                                         new SingleBlockWorldReader(blockState, positionSupplier.get(), this.worldReaderSupplier.get()),
                                                         this.positionSupplier.get()));
                                                     return i < this.worldReaderSupplier.get().getMaxLightLevel();
                                                 }
                                             });
            }
        }

        private void onBlockStateRemoved(final BlockState blockState, final BlockPos pos) {
            skylightBlockingBits.set(pos.getY(), !ILevelBasedPropertyAccessor.getInstance().propagatesSkylightDown(
              new SingleBlockBlockReader(blockState, positionSupplier.get(), this.worldReaderSupplier.get()),
              positionSupplier.get()
            ));

            if (!skylightBlockingBits.get(pos.getY()))
            {
                canPropagateSkylightDown = IntStream.range(0, StateEntrySize.current().getBitsPerBlockSide())
                                             .noneMatch(skylightBlockingBits::get);
            }

            if (pos.getY() >= highestBit) {
                highestBit = -1;
                highestBitFriction = 0f;

                for (int i = StateEntrySize.current().getBitsPerBlockSide() - 1; i >= 0; i--)
                {
                    if (noneAirBits.get(i)) {
                        highestBit = (short) i;
                        highestBitFriction = ILevelBasedPropertyAccessor.getInstance().getFriction(
                          new SingleBlockWorldReader(ChiseledBlockEntity.this.compressedSection.getBlockState(pos.getX(), i, pos.getZ()), positionSupplier.get(), this.worldReaderSupplier.get()),
                          positionSupplier.get(),
                          null
                        );
                        break;
                    }
                }
            }

            if (pos.getY() == 0) {
                this.canLowestBitSustainGrass = true;
            }
        }

        private void onBlockStateReplaced(final BlockState currentState, final BlockState newState, final BlockPos pos) {
            onBlockStateRemoved(currentState, pos);
            onBlockStateAdded(newState, pos);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag compoundTag = new CompoundTag();

            compoundTag.putByteArray(NbtConstants.SKYLIGHT_BLOCKING_BITS, skylightBlockingBits.toByteArray());
            compoundTag.putByteArray(NbtConstants.NONE_AIR_BITS, noneAirBits.toByteArray());
            compoundTag.putShort(NbtConstants.HIGHEST_BIT, highestBit);
            compoundTag.putFloat(NbtConstants.HIGHEST_BIT_FRICTION, highestBitFriction);
            compoundTag.putBoolean(NbtConstants.CAN_PROPAGATE_SKYLIGHT_DOWN, canPropagateSkylightDown);
            compoundTag.putBoolean(NbtConstants.LOWEST_BIT_CAN_SUSTAIN_GRASS, canLowestBitSustainGrass);

            return compoundTag;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            skylightBlockingBits.clear();
            skylightBlockingBits.or(BitSet.valueOf(nbt.getByteArray(NbtConstants.SKYLIGHT_BLOCKING_BITS)));

            noneAirBits.clear();
            noneAirBits.or(BitSet.valueOf(nbt.getByteArray(NbtConstants.NONE_AIR_BITS)));

            highestBit = nbt.getShort(NbtConstants.HIGHEST_BIT);
            highestBitFriction = nbt.getFloat(NbtConstants.HIGHEST_BIT_FRICTION);
            canPropagateSkylightDown = nbt.getBoolean(NbtConstants.CAN_PROPAGATE_SKYLIGHT_DOWN);
            canLowestBitSustainGrass = nbt.getBoolean(NbtConstants.LOWEST_BIT_CAN_SUSTAIN_GRASS);
        }

        @Override
        public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
        {
            packetBuffer.writeBitSet(skylightBlockingBits);
            packetBuffer.writeBitSet(noneAirBits);
            packetBuffer.writeShort(highestBit);
            packetBuffer.writeFloat(highestBitFriction);
            packetBuffer.writeBoolean(canPropagateSkylightDown);
            packetBuffer.writeBoolean(canLowestBitSustainGrass);
        }

        @Override
        public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
        {
            skylightBlockingBits.clear();
            skylightBlockingBits.or(packetBuffer.readBitSet());

            noneAirBits.clear();
            noneAirBits.or(packetBuffer.readBitSet());

            highestBit = packetBuffer.readShort();
            highestBitFriction = packetBuffer.readFloat();
            canPropagateSkylightDown = packetBuffer.readBoolean();
            canLowestBitSustainGrass = packetBuffer.readBoolean();
        }

        public void initializeWith(final BlockState blockState)
        {
            skylightBlockingBits.clear();
            noneAirBits.clear();

            skylightBlockingBits.set(0, StateEntrySize.current().getBitsPerBlockSide(),
              !ILevelBasedPropertyAccessor.getInstance().propagatesSkylightDown(
                new SingleBlockBlockReader(blockState, positionSupplier.get(), this.worldReaderSupplier.get()),
                positionSupplier.get()
              ));
            noneAirBits.set(0, !blockState.isAir());

            if (blockState.isAir()) {
                highestBit = -1;
                highestBitFriction = 0f;
            }
            else
            {
                highestBit = (short) (StateEntrySize.current().getBitsPerBlockSide() - 1);
                highestBitFriction = ILevelBasedPropertyAccessor.getInstance().getFriction(
                  new SingleBlockWorldReader(blockState, positionSupplier.get(), this.worldReaderSupplier.get()),
                  positionSupplier.get(),
                  null
                );
            }

            this.canPropagateSkylightDown = ILevelBasedPropertyAccessor.getInstance().propagatesSkylightDown(
              new SingleBlockBlockReader(blockState, positionSupplier.get(), this.worldReaderSupplier.get()),
              positionSupplier.get()
            );

            this.canLowestBitSustainGrass = blockState.isAir() || ILevelBasedPropertyAccessor.getInstance()
              .canBeGrass(
                new SingleBlockWorldReader(blockState, positionSupplier.get(), this.worldReaderSupplier.get()),
                Blocks.GRASS_BLOCK.defaultBlockState(),
                positionSupplier.get().below(),
                blockState,
                positionSupplier.get()
              )
              .orElseGet(() -> {
                  if (blockState.is(Blocks.SNOW) && blockState.getValue(SnowLayerBlock.LAYERS) == 1) {
                      return true;
                  } else if (blockState.getFluidState().getAmount() == 8) {
                      return false;
                  } else {
                      int i = LayerLightEngine.getLightBlockInto(
                        new SingleBlockWorldReader(blockState, positionSupplier.get(), this.worldReaderSupplier.get()),
                        Blocks.GRASS_BLOCK.defaultBlockState(),
                        this.positionSupplier.get().below(),
                        blockState,
                        this.positionSupplier.get(),
                        Direction.UP,
                        blockState.getLightBlock(
                          new SingleBlockWorldReader(blockState, positionSupplier.get(), this.worldReaderSupplier.get()),
                          this.positionSupplier.get()));
                      return i < this.worldReaderSupplier.get().getMaxLightLevel();
                  }
              });
        }
    }

    private static final class Identifier implements IByteArrayBackedAreaShapeIdentifier
    {

        private final byte[] identifyingPayload;

        private Identifier(final IStateEntryStorage section)
        {
            this.identifyingPayload = Arrays.copyOf(
              section.getRawData(),
              section.getRawData().length
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
            if (!(o instanceof final IByteArrayBackedAreaShapeIdentifier that))
            {
                return false;
            }
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
        public byte[] getBackingData()
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

    @SuppressWarnings("removal")
    private final class LegacyChunkSectionBasedStorageHandler implements ILegacyStorageHandler
    {
        @Override
        public boolean matches(final @NotNull CompoundTag compoundTag)
        {
            return compoundTag.contains(NbtConstants.CHISEL_BLOCK_ENTITY_DATA);
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            final CompoundTag chiselBlockData =  nbt.getCompound(NbtConstants.CHISEL_BLOCK_ENTITY_DATA);
            final CompoundTag compressedSectionData = chiselBlockData.getCompound(NbtConstants.COMPRESSED_STORAGE);

            final LevelChunkSection chunkSection = new LevelChunkSection(0, BuiltinRegistries.BIOME);
            ChunkSectionUtils.deserializeNBT(
              chunkSection,
              compressedSectionData
            );

            compressedSection.loadFromChunkSection(chunkSection);

            if (chiselBlockData.contains(NbtConstants.STATISTICS)) {
                final CompoundTag statisticsData = chiselBlockData.getCompound(NbtConstants.STATISTICS);
                mutableStatistics.deserializeNBT(statisticsData);
            }
            else
            {
                mutableStatistics.recalculate(compressedSection, false);
            }
        }
    }

    @SuppressWarnings("removal")
    private final class LegacyGZIPStorageBasedStorageHandler implements ILegacyStorageHandler
    {
        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            GZIPDataCompressionUtils.decompress(nbt, compoundTag -> {
                compressedSection.deserializeNBT(compoundTag.getCompound(NbtConstants.CHISELED_DATA));
                mutableStatistics.deserializeNBT(compoundTag.getCompound(NbtConstants.STATISTICS));

                if (mutableStatistics.isRequiresRecalculation())
                {
                    mutableStatistics.recalculate(compressedSection, false);
                }
            });
        }

        @Override
        public boolean matches(final @NotNull CompoundTag compoundTag)
        {
            return true; //The last of the legacy ones
        }
    }

    private final class LZ4StorageBasedStorageHandler implements IStorageHandler
    {

        @Override
        public CompoundTag serializeNBT()
        {
            return LZ4DataCompressionUtils.compress(compoundTag -> {
                compoundTag.put(NbtConstants.CHISELED_DATA, compressedSection.serializeNBT());
                compoundTag.put(NbtConstants.STATISTICS, mutableStatistics.serializeNBT());

                if (mutableStatistics.isRequiresRecalculation())
                {
                    mutableStatistics.recalculate(compressedSection);
                }
            });
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            LZ4DataCompressionUtils.decompress(nbt, compoundTag -> {
                compressedSection.deserializeNBT(compoundTag.getCompound(NbtConstants.CHISELED_DATA));
                mutableStatistics.deserializeNBT(compoundTag.getCompound(NbtConstants.STATISTICS));

                if (mutableStatistics.isRequiresRecalculation())
                {
                    mutableStatistics.recalculate(compressedSection, getLevel() != null);
                }

                mutableStatistics.updatePrimaryState(getLevel() != null);
            });
        }
    }
}
