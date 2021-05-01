package mod.chiselsandbits.legacy;

import io.netty.buffer.Unpooled;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.legacy.serialization.blob.*;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.InflaterInputStream;

public class LegacyLoadManager
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final LegacyLoadManager INSTANCE = new LegacyLoadManager();

    public static final int VERSION_COMPACT           = 0; // stored meta.
    public static final int VERSION_CROSSWORLD        = 2;
    public static final int VERSION_COMPACT_PALLETED_BROKEN = 3;
    public static final int VERSION_COMPACT_PALLETED = 4;

    private final static int ARRAY_SIZE = 16*16*16;

    private static final String LEGACY_BYTE_VOXEL_ARRAY_KEY = "X";

    private static final ChunkSection EMPTY_SECTION = new ChunkSection(0);

    public static LegacyLoadManager getInstance()
    {
        return INSTANCE;
    }

    private LegacyLoadManager()
    {
    }

    public ChunkSection attemptLegacyBlockEntityLoad(final CompoundNBT entityNbt) {
        if (!entityNbt.contains(LEGACY_BYTE_VOXEL_ARRAY_KEY))
            return EMPTY_SECTION;

        final byte[] legacyVoxelData = entityNbt.getByteArray(LEGACY_BYTE_VOXEL_ARRAY_KEY);
        if (legacyVoxelData.length == 0)
            return EMPTY_SECTION;

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(legacyVoxelData);

        try
        {
            return attemptLegacyBlockEntityLoad(byteArrayInputStream);
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to read the legacy data from the byte array. Dropping TE Data.", e);
            return EMPTY_SECTION;
        }
    }

    private ChunkSection attemptLegacyBlockEntityLoad(final ByteArrayInputStream byteArrayInputStream) throws IOException
    {
        final InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
        final ByteBuffer byteBuffer = ByteBuffer.allocate(3145728);

        int usedBytes = 0;
        int remainingBytes = 0;

        do
        {
            usedBytes += remainingBytes;
            remainingBytes = inflaterInputStream.read(byteBuffer.array(), usedBytes, byteBuffer.limit() - usedBytes);
        }
        while (remainingBytes > 0);

        final PacketBuffer packetBuffer = new PacketBuffer(Unpooled.wrappedBuffer(byteBuffer));
        return attemptLegacyBlockEntityLoad(packetBuffer, byteBuffer);
    }

    private ChunkSection attemptLegacyBlockEntityLoad(final PacketBuffer packetBuffer, final ByteBuffer byteBuffer) {
        final int packagedVersion = packetBuffer.readInt();
        final BlobSerializer serializer = getBlobSerializerFromVersion(packagedVersion, packetBuffer);

        final int byteOffset = packetBuffer.readInt();
        final int bytesOfInterest = packetBuffer.readInt();

        final BitStream bits = BitStream.valueOf(byteOffset, ByteBuffer.wrap(byteBuffer.array(), packetBuffer.readerIndex(), bytesOfInterest));
        final ChunkSection resultingSection = new ChunkSection(0);

        for (int i = 0; i < ARRAY_SIZE; i++)
        {
            final int stateId = serializer.readVoxelStateID(bits);
            final BlockState state = IBlockStateIdManager.getInstance().getBlockStateFrom(stateId);

            final BlockPos pos = getPositionFromDataIndex(i);

            resultingSection.setBlockState(pos.getX(), pos.getY(), pos.getZ(), state, false);
        }

        return resultingSection;
    }

    private BlobSerializer getBlobSerializerFromVersion(final int version, final PacketBuffer source) {
        if (version == VERSION_COMPACT)
        {
            return new BlobSerializer(source);
        }
        else if (version == VERSION_COMPACT_PALLETED_BROKEN)
        {
            return new PalettedBlobSerializer(source);
        }
        else if (version == VERSION_COMPACT_PALLETED)
        {
            return new NbtBasedPalettedBlobSerializer(source);
        }
        else if (version == VERSION_CROSSWORLD)
        {
            return new CrossWorldBlobSerializer(source);
        }
        else
        {
            throw new RuntimeException("Invalid Version: " + version);
        }
    }

    private static BlockPos getPositionFromDataIndex(final int dataIndex) {
        final int x = dataIndex & 15;
        final int y = (dataIndex >> 4) & 15;
        final int z = (dataIndex >> 8) & 15;
        
        return new BlockPos(x, y, z);
    }
}
