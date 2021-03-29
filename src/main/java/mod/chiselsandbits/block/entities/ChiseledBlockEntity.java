package mod.chiselsandbits.block.entities;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockStatistics;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.world.IInWorldStateEntryInfo;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import mod.chiselsandbits.api.util.PacketBufferCache;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import mod.chiselsandbits.utils.SingleBlockWorldReader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ChiseledBlockEntity extends TileEntity implements IMultiStateBlockEntity
{
    public static final int   BITS_PER_BLOCK_SIDE = 16;
    public static final int   BITS_PER_BLOCK = BITS_PER_BLOCK_SIDE * BITS_PER_BLOCK_SIDE * BITS_PER_BLOCK_SIDE;
    public static final int   BITS_PER_LAYER = BITS_PER_BLOCK_SIDE * BITS_PER_BLOCK_SIDE;
    public static final float SIZE_PER_BIT        = 1/16f;
    public static final float ONE_THOUSANDS = 1 / 1000f;

    private final ChunkSection compressedSection;
    private final MutableStatistics mutableStatistics;

    public ChiseledBlockEntity(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
        compressedSection = new ChunkSection(0); //We always use a minimal y level to lookup. Makes calculations internally easier.
        mutableStatistics = new MutableStatistics(this::getWorld);
    }

    @Override
    public Stream<IStateEntryInfo> stream()
    {
        return BlockPosStreamProvider.getForRange(BITS_PER_BLOCK_SIDE)
          .map(blockPos -> new StateEntry(
            compressedSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
            getWorld(),
            getPos(),
            blockPos
          )
      );
    }

    @Override
    public IMultiStateSnapshot createSnapshot()
    {
        return MultiStateSnapshotUtils.createFromSection(this.compressedSection);
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        super.deserializeNBT(nbt);

        final CompoundNBT chiselBlockData = nbt.getCompound(NbtConstants.CHISEL_BLOCK_ENTITY_DATA);
        final CompoundNBT compressedSectionData = chiselBlockData.getCompound(NbtConstants.COMPRESSED_STORAGE);
        final CompoundNBT statisticsData = chiselBlockData.getCompound(NbtConstants.STATISTICS);

        ChunkSectionUtils.deserializeNBT(
          this.compressedSection,
          compressedSectionData
        );

        mutableStatistics.deserializeNBT(statisticsData);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = super.serializeNBT();

        final CompoundNBT chiselBlockData = new CompoundNBT();
        final CompoundNBT compressedSectionData = ChunkSectionUtils.serializeNBT(this.compressedSection);
        chiselBlockData.put(NbtConstants.COMPRESSED_STORAGE, compressedSectionData);
        chiselBlockData.put(NbtConstants.STATISTICS, mutableStatistics.serializeNBT());

        nbt.put(NbtConstants.CHISEL_BLOCK_ENTITY_DATA, chiselBlockData);

        return nbt;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(pos, 255, getUpdateTag());
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        //Special compound version which just contains the bit array!
        final CompoundNBT updateTag = new CompoundNBT();

        final PacketBuffer packetBuffer = PacketBufferCache.getInstance().get();
        this.serializeInto(packetBuffer);
        updateTag.putByteArray(NbtConstants.CHISEL_BLOCK_ENTITY_DATA, packetBuffer.array());

        return updateTag;
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket pkt)
    {
        handleUpdateTag(Objects.requireNonNull(getWorld()).getBlockState(getPos()), pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(final BlockState state, final CompoundNBT tag)
    {
        final byte[] compressedStorageData = tag.getByteArray(NbtConstants.CHISEL_BLOCK_ENTITY_DATA);
        final PacketBuffer packetBuffer = PacketBufferCache.getInstance().get();
        packetBuffer.writeByteArray(compressedStorageData);

        packetBuffer.resetReaderIndex();
        packetBuffer.resetWriterIndex();

        this.deserializeFrom(packetBuffer);
    }

    @Override
    public void serializeInto(@NotNull final PacketBuffer packetBuffer)
    {
        compressedSection.getData().write(packetBuffer);
        mutableStatistics.serializeInto(packetBuffer);
    }

    @Override
    public void deserializeFrom(@NotNull final PacketBuffer packetBuffer)
    {
        compressedSection.getData().read(packetBuffer);
        mutableStatistics.deserializeFrom(packetBuffer);
    }

    @Override
    public Vector3d getStartPoint()
    {
        return Vector3d.copy(getPos());
    }

    @Override
    public Vector3d getEndPoint()
    {
        return getStartPoint().add(1, 1, 1).subtract(ONE_THOUSANDS, ONE_THOUSANDS, ONE_THOUSANDS);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setInAreaTarget(final BlockState blockState, final Vector3d inAreaTarget) throws SpaceOccupiedException
    {
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0 ||
              inAreaTarget.getX() >= 1 ||
              inAreaTarget.getY() >= 1 ||
              inAreaTarget.getZ() >= 1) {
            throw new IllegalArgumentException("Target is not in the current area.");
        }

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE));

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
            mutableStatistics.onBlockStateRemoved(currentState, inAreaPos);
        } else if (!blockState.isAir() && currentState.isAir()) {
            mutableStatistics.onBlockStateAdded(blockState, inAreaPos);
        } else if (!blockState.isAir() && !currentState.isAir()) {
            mutableStatistics.onBlockStateReplaced(currentState, blockState, inAreaPos);
        }
    }

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

    @Override
    public IMultiStateBlockStatistics getStatistics()
    {
        return mutableStatistics;
    }

    private static final class StateEntry implements IInWorldStateEntryInfo {

        private final BlockState state;
        private final IBlockReader reader;
        private final BlockPos blockPos;
        private final Vector3d startPoint;
        private final Vector3d endPoint;

        public StateEntry(final BlockState state, final IBlockReader reader, final BlockPos blockPos, final Vector3i startPoint)
        {
            this(
              state,
              reader,
              blockPos,
              Vector3d.copy(startPoint).mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT),
              Vector3d.copy(startPoint).mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT).add(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT)
            );
        }

        private StateEntry(
          final BlockState state,
          final IBlockReader reader,
          final BlockPos blockPos,
          final Vector3d startPoint,
          final Vector3d endPoint) {
            this.state = state;
            this.reader = reader;
            this.blockPos = blockPos;
            this.startPoint = startPoint;
            this.endPoint = endPoint;
        }

        @Override
        public BlockState getState()
        {
            return state;
        }

        @Override
        public IBlockReader getWorld()
        {
            return reader;
        }

        @Override
        public BlockPos getBlockPos()
        {
            return blockPos;
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
    }

    private static final class MutableStatistics implements IMultiStateBlockStatistics, INBTSerializable<CompoundNBT>, IPacketBufferSerializable {

        private final Supplier<IWorldReader> worldReaderSupplier;

        private BlockState primaryState = Blocks.AIR.getDefaultState();
        private final Map<BlockState, Integer> countMap = Maps.newConcurrentMap();

        private int totalUsedBlockCount = 0;
        private int totalUsedChecksWeakPowerCount = 0;
        private float totalUpperSurfaceSlipperiness = 0f;

        private MutableStatistics(final Supplier<IWorldReader> worldReaderSupplier) {this.worldReaderSupplier = worldReaderSupplier;}

        @Override
        public BlockState getPrimaryState()
        {
            return primaryState;
        }

        @Override
        public Map<BlockState, Integer> getStateCounts()
        {
            return Collections.unmodifiableMap(countMap);
        }

        @Override
        public boolean shouldCheckWeakPower() {
            return totalUsedChecksWeakPowerCount == BITS_PER_BLOCK;
        }

        @Override
        public float getFullnessFactor() {
            return totalUsedBlockCount / (float) BITS_PER_BLOCK;
        }

        @Override
        public float getSlipperiness()
        {
            return totalUpperSurfaceSlipperiness / (float) BITS_PER_LAYER;
        }

        private void onBlockStateAdded(final BlockState blockState, final BlockPos pos) {
            countMap.putIfAbsent(blockState, 0);
            countMap.computeIfPresent(blockState, (state, currentCount) -> currentCount + 1);
            updatePrimaryState();

            this.totalUsedBlockCount++;

            if (blockState.shouldCheckWeakPower(
              new SingleBlockWorldReader(
                blockState,
                this.worldReaderSupplier.get()
                ),
              BlockPos.ZERO,
              Direction.NORTH
            )) {
                this.totalUsedChecksWeakPowerCount++;
            }

            if (pos.getY() == 15) {
                this.totalUpperSurfaceSlipperiness += blockState.getSlipperiness(
                  new SingleBlockWorldReader(
                    blockState,
                    this.worldReaderSupplier.get()
                  ),
                  BlockPos.ZERO,
                  null
                );
            }
        }

        private void onBlockStateRemoved(final BlockState blockState, final BlockPos pos) {
            countMap.computeIfPresent(blockState, (state, currentCount) -> currentCount - 1);
            countMap.remove(blockState, 0);
            updatePrimaryState();

            this.totalUsedBlockCount--;

            if (blockState.shouldCheckWeakPower(
              new SingleBlockWorldReader(
                blockState,
                this.worldReaderSupplier.get()
              ),
              BlockPos.ZERO,
              Direction.NORTH
            )) {
                this.totalUsedChecksWeakPowerCount--;
            }

            if (pos.getY() == 15) {
                this.totalUpperSurfaceSlipperiness -= blockState.getSlipperiness(
                  new SingleBlockWorldReader(
                    blockState,
                    this.worldReaderSupplier.get()
                  ),
                  BlockPos.ZERO,
                  null
                );
            }
        }

        private void onBlockStateReplaced(final BlockState currentState, final BlockState newState, final BlockPos pos) {
            countMap.computeIfPresent(currentState, (state, currentCount) -> currentCount - 1);
            countMap.remove(currentState, 0);
            countMap.putIfAbsent(newState, 0);
            countMap.computeIfPresent(newState, (state, currentCount) -> currentCount + 1);
            updatePrimaryState();

            if (currentState.shouldCheckWeakPower(
              new SingleBlockWorldReader(
                currentState,
                this.worldReaderSupplier.get()
              ),
              BlockPos.ZERO,
              Direction.NORTH
            )) {
                this.totalUsedChecksWeakPowerCount--;
            }

            if (newState.shouldCheckWeakPower(
              new SingleBlockWorldReader(
                newState,
                this.worldReaderSupplier.get()
              ),
              BlockPos.ZERO,
              Direction.NORTH
            )) {
                this.totalUsedChecksWeakPowerCount++;
            }

            if (pos.getY() == 15) {
                this.totalUpperSurfaceSlipperiness -= currentState.getSlipperiness(
                  new SingleBlockWorldReader(
                    currentState,
                    this.worldReaderSupplier.get()
                  ),
                  BlockPos.ZERO,
                  null
                );

                this.totalUpperSurfaceSlipperiness += newState.getSlipperiness(
                  new SingleBlockWorldReader(
                    newState,
                    this.worldReaderSupplier.get()
                  ),
                  BlockPos.ZERO,
                  null
                );
            }
        }

        private void updatePrimaryState() {
            primaryState = this.countMap.entrySet().stream().min((o1, o2) -> -1 * (o1.getValue() - o2.getValue()))
              .map(Map.Entry::getKey)
              .orElseGet(Blocks.AIR::getDefaultState);
        }

        @Override
        public void serializeInto(@NotNull final PacketBuffer packetBuffer)
        {
            packetBuffer.writeVarInt(ModUtil.getStateId(this.primaryState));
            packetBuffer.writeVarInt(this.countMap.size());
            for (final Map.Entry<BlockState, Integer> blockStateIntegerEntry : this.countMap.entrySet())
            {
                packetBuffer.writeVarInt(ModUtil.getStateId(blockStateIntegerEntry.getKey()));
                packetBuffer.writeVarInt(blockStateIntegerEntry.getValue());
            }

            packetBuffer.writeVarInt(this.totalUsedBlockCount);
            packetBuffer.writeVarInt(this.totalUsedChecksWeakPowerCount);
        }

        @Override
        public void deserializeFrom(@NotNull final PacketBuffer packetBuffer)
        {
            this.countMap.clear();

            this.primaryState = ModUtil.getStateById(packetBuffer.readVarInt());

            final int stateCount = packetBuffer.readVarInt();
            for (int i = 0; i < stateCount; i++)
            {
                this.countMap.put(
                  ModUtil.getStateById(packetBuffer.readVarInt()),
                  packetBuffer.readVarInt()
                );
            }

            this.totalUsedBlockCount = packetBuffer.readVarInt();
            this.totalUsedChecksWeakPowerCount = packetBuffer.readVarInt();
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

            nbt.putInt(NbtConstants.TOTAL_BLOCK_COUNT, totalUsedBlockCount);
            nbt.putInt(NbtConstants.TOTAL_SHOULD_CHECK_WEAK_POWER_COUNT, totalUsedChecksWeakPowerCount);

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

            this.totalUsedBlockCount = nbt.getInt(NbtConstants.TOTAL_BLOCK_COUNT);
            this.totalUsedChecksWeakPowerCount = nbt.getInt(NbtConstants.TOTAL_SHOULD_CHECK_WEAK_POWER_COUNT);
        }
    }
}
