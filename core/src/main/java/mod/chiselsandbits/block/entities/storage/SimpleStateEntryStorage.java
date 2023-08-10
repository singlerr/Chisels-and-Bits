package mod.chiselsandbits.block.entities.storage;

import com.google.common.collect.Maps;
import com.google.common.math.LongMath;
import com.mojang.datafixers.util.Pair;
import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.util.IBatchMutation;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.api.config.IServerConfiguration;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.utils.ByteArrayUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.util.*;
import java.util.function.BiConsumer;

public class SimpleStateEntryStorage implements IStateEntryStorage
{

    private static final Logger LOGGER = LogManager.getLogger();

    private final int  size;
    private final SimpleStateEntryPalette palette;

    private BitSet data       = new BitSet();
    private int    entryWidth = 0;
    private boolean isDeserializing = false;
    private List<IBatchMutation> ongoingBatchMutations = new ArrayList<>();

    public SimpleStateEntryStorage()
    {
        this(IServerConfiguration.getInstance().getBitSize().get().getBitsPerBlockSide());
    }

    @SuppressWarnings("CopyConstructorMissesField")
    private SimpleStateEntryStorage(final SimpleStateEntryStorage stateEntryStorage) {
        this.size = stateEntryStorage.size;
        this.palette = new SimpleStateEntryPalette(this::onPaletteResize, this::onPaletteIndexChanged, stateEntryStorage.palette);
        this.data = stateEntryStorage.data;
        this.entryWidth = stateEntryStorage.entryWidth;
    }

    public SimpleStateEntryStorage(final int size) {
        this.size = size;
        this.palette = new SimpleStateEntryPalette(this::onPaletteResize, this::onPaletteIndexChanged);
    }

    @Override
    public int getSize()
    {
        return size;
    }

    private int getTotalEntryCount() {
        return size * size * size;
    }

    @Override
    public void clear()
    {
        this.data = new BitSet();
        this.entryWidth = 0;
        this.palette.clear();
    }

    private void resetData() {
        this.data = new BitSet();
    }

    @Override
    public void initializeWith(final IBlockInformation currentState)
    {
        clear();
        if (currentState.getBlockState() == Blocks.AIR.defaultBlockState())
        {
            return;
        }

        final int blockStateId = palette.getIndex(currentState);
        this.data = ByteArrayUtils.fill(blockStateId, entryWidth, getTotalEntryCount());
    }

    @Override
    public void loadFromChunkSection(final LevelChunkSection chunkSection)
    {
        if (this.size != StateEntrySize.ONE_SIXTEENTH.getBitsPerBlockSide())
            throw new IllegalStateException("Updating to the new storage format is only possible on the default 1/16th size.");

        this.clear();

        try(IBatchMutation ignored = batch()) {
            BlockPosStreamProvider.getForRange(StateEntrySize.ONE_SIXTEENTH.getBitsPerBlockSide())
                    .forEach(position -> setBlockInformation(
                            position.getX(),
                            position.getY(),
                            position.getZ(),
                            new BlockInformation(
                                    chunkSection.getBlockState(position.getX(), position.getY(), position.getZ()),
                                    Optional.empty()
                            )
                    ));
        }
    }

    @Override
    public IBlockInformation getBlockInformation(final int x, final int y, final int z)
    {
        final int offSetIndex = doCalculatePositionIndex(x, y, z);
        final int blockStateId = ByteArrayUtils.getValueAt(data, entryWidth, offSetIndex);

        return palette.getBlockState(blockStateId);
    }

    @Override
    public void setBlockInformation(final int x, final int y, final int z, final IBlockInformation blockState)
    {
        final int offSetIndex = doCalculatePositionIndex(x, y, z);
        final int blockStateId = palette.getIndex(blockState);

        ensureCapacity();

        ByteArrayUtils.setValueAt(data, blockStateId, entryWidth, offSetIndex);
    }

    private void ensureCapacity() {
        final int requiredSize = (int) Math.ceil((getTotalEntryCount() * entryWidth) / (float) Byte.SIZE);
        if (data.length() < requiredSize) {
            final byte[] rawData = getRawData();
            final byte[] newData = new byte[requiredSize];
            System.arraycopy(rawData, 0, newData, 0, rawData.length);
            this.data = BitSet.valueOf(newData);
        } else if (this.ongoingBatchMutations.isEmpty()) {
            this.data = BitSet.valueOf(getRawData());
        }
    }

    private int doCalculatePositionIndex(final int x, final int y, final int z)
    {
        return x * size * size + y * size + z;
    }

    private Vec3i doCalculatePosition(final int index) {
        final int x = index / (size * size);
        final int y = (index - x * size * size) / size;
        final int z = index - x * size * size - y * size;

        return new Vec3i(x, y, z);
    }

    @Override
    public void count(final BiConsumer<IBlockInformation, Integer> storageConsumer)
    {
        final Map<IBlockInformation, Integer> countMap = Maps.newHashMap();

        BlockPosStreamProvider.getForRange(this.getSize())
          .map(position -> getBlockInformation(position.getX(), position.getY(), position.getZ()))
          .forEach(blockState -> countMap.compute(blockState, (state, count) -> count == null ? 1 : count + 1));

        countMap.forEach(storageConsumer);
    }

    public BitSet getData()
    {
        return data;
    }

    @Override
    public byte[] getRawData()
    {
        return this.data.toByteArray();
    }

    @Override
    public IStateEntryStorage createSnapshot()
    {
        return new SimpleStateEntryStorage(this);
    }

    @Override
    public void fillFromBottom(final IBlockInformation state, final int entries)
    {
        clear();
        final int loopCount = Math.max(0, Math.min(entries, StateEntrySize.current().getBitsPerBlock()));
        if (loopCount == 0)
            return;

        int count = 0;
        try(IBatchMutation ignored = batch()) {
            for (int y = 0; y < getSize(); y++)
            {
                for (int x = 0; x < getSize(); x++)
                {
                    for (int z = 0; z < getSize(); z++)
                    {
                        setBlockInformation(
                                x, y, z,
                                state
                        );

                        count++;
                        if (count == loopCount)
                            return;
                    }
                }
            }
        }
    }

    @Override
    public List<IBlockInformation> getContainedPalette()
    {
        return palette.getStates();
    }

    @Override
    public void rotate(final Direction.Axis axis, final int rotationCount)
    {
        if (rotationCount == 0)
            return;

        final IStateEntryStorage clone = this.createSnapshot();
        resetData();

        final Vec3 centerVector = new Vec3(7.5d, 7.5d, 7.5d);

        try(IBatchMutation ignored = batch()) {
            for (int x = 0; x < 16; x++)
            {
                for (int y = 0; y < 16; y++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        final Vec3 workingVector = new Vec3(x, y, z);
                        Vec3 rotatedVector = workingVector.subtract(centerVector);
                        for (int i = 0; i < rotationCount; i++)
                        {
                            rotatedVector = VectorUtils.rotate90Degrees(rotatedVector, axis);
                        }

                        final BlockPos sourcePos = VectorUtils.toBlockPos(workingVector);
                        final Vec3 offsetPos = rotatedVector.add(centerVector).multiply(1000,1000,1000);
                        final Vec3 exactTargetPos = new Vec3(Math.round(offsetPos.x()), Math.round(offsetPos.y()), Math.round(offsetPos.z())).multiply(1/1000d,1/1000d,1/1000d);
                        final BlockPos targetPos = VectorUtils.toBlockPos(exactTargetPos);

                        this.setBlockInformation(
                                targetPos.getX(),
                                targetPos.getY(),
                                targetPos.getZ(),
                                clone.getBlockInformation(
                                        sourcePos.getX(),
                                        sourcePos.getY(),
                                        sourcePos.getZ()
                                )
                        );
                    }
                }
            }
        }
    }

    @Override
    public void mirror(final Direction.Axis axis)
    {
        final IStateEntryStorage clone = this.createSnapshot();
        resetData();

        try (IBatchMutation ignored = batch()) {
            for (int y = 0; y < getSize(); y++)
            {
                for (int x = 0; x < getSize(); x++)
                {
                    for (int z = 0; z < getSize(); z++)
                    {
                        final IBlockInformation blockInformation = clone.getBlockInformation(x, y, z);

                        final int mirroredX = axis == Direction.Axis.X ? (getSize() - x - 1) : x;
                        final int mirroredY = axis == Direction.Axis.Y ? (getSize() - y - 1) : y;
                        final int mirroredZ = axis == Direction.Axis.Z ? (getSize() - z - 1) : z;

                        this.setBlockInformation(
                                mirroredX, mirroredY, mirroredZ,
                                blockInformation
                        );
                    }
                }
            }
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag result = new CompoundTag();

        result.put(NbtConstants.PALETTE, this.palette.serializeNBT());
        result.putByteArray(NbtConstants.DATA, this.getRawData());

        return result;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        clear();

        this.isDeserializing = true;

        if (!nbt.contains(NbtConstants.PALETTE) || !nbt.contains(NbtConstants.DATA)) {
            LOGGER.error("The given NBT tag does not contain the required data for deserialization of a simple state entry storage. NBT: %s".formatted(nbt));
            this.isDeserializing = false;
            return;
        }

        this.palette.deserializeNBT((ListTag) Objects.requireNonNull(nbt.get(NbtConstants.PALETTE)));
        this.data = BitSet.valueOf(nbt.getByteArray(NbtConstants.DATA));

        final Set<IBlockInformation> containedStates = new HashSet<>();
        for (int i = 0; i < getTotalEntryCount(); i++)
        {
            final Vec3i pos = doCalculatePosition(i);
            final IBlockInformation blockState = getBlockInformation(pos);
            containedStates.add(blockState);
        }

        final List<IBlockInformation> paletteStates = new ArrayList<>(this.palette.getStates());
        paletteStates.removeAll(containedStates);
        paletteStates.remove(BlockInformation.AIR); //We need to keep this!

        this.isDeserializing = false;

        this.palette.sanitize(paletteStates);
    }

    @Override
    public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
    {
        this.palette.serializeInto(packetBuffer);
        packetBuffer.writeByteArray(this.data.toByteArray());
    }

    @Override
    public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
    {
        clear();

        this.isDeserializing = true;

        this.palette.deserializeFrom(packetBuffer);
        this.data = BitSet.valueOf(packetBuffer.readByteArray());

        this.isDeserializing = false;
    }

    private void onPaletteResize(final int newSize) {
        final int currentEntryWidth = this.entryWidth;
        this.entryWidth = LongMath.log2(newSize, RoundingMode.CEILING);

        if (!this.isDeserializing && this.entryWidth != currentEntryWidth) {
            //We need to update the data array to match the new palette size
            final BitSet rawData = this.data;

            this.data = new BitSet(getTotalEntryCount() * entryWidth);
            BlockPosStreamProvider.getForRange(getSize())
              .mapToInt(pos -> doCalculatePositionIndex(pos.getX(), pos.getY(), pos.getZ()))
              .mapToObj(index -> Pair.of(index, ByteArrayUtils.getValueAt(rawData, currentEntryWidth, index)))
              .forEach(pair -> ByteArrayUtils.setValueAt(this.data, pair.getSecond(), this.entryWidth, pair.getFirst()));
        }
    }

    private void onPaletteIndexChanged(final Map<Integer, Integer> remaps) {
        if (remaps.isEmpty())
            return;

        for (int i = 0; i < getTotalEntryCount(); i++)
        {
            final int currentId = ByteArrayUtils.getValueAt(data, entryWidth, i);
            if (remaps.containsKey(currentId)) {
                ByteArrayUtils.setValueAt(data, remaps.get(currentId), entryWidth, i);
            }
        }
    }

    @Override
    public IBatchMutation batch() {
        final IBatchMutation mutation = new IBatchMutation() {
            @Override
            public void close() {
                SimpleStateEntryStorage.this.ongoingBatchMutations.remove(this);
            }
        };

        ongoingBatchMutations.add(mutation);

        this.data = BitSet.valueOf(this.data.toLongArray());

        return mutation;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof final SimpleStateEntryStorage that))
        {
            return false;
        }

        if (entryWidth != that.entryWidth)
        {
            return false;
        }
        if (!palette.equals(that.palette))
        {
            return false;
        }
        return getData().equals(that.getData());
    }

    @Override
    public int hashCode()
    {
        int result = palette.hashCode();
        result = 31 * result + getData().hashCode();
        result = 31 * result + entryWidth;
        return result;
    }

    @Override
    public String toString()
    {
        return "SimpleStateEntryStorage{" +
                 "palette=" + palette +
                 ", data=" + data +
                 ", entryWidth=" + entryWidth +
                 '}';
    }
}
