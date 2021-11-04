package mod.chiselsandbits.utils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;

public class ChunkSectionUtils
{

    private static final Logger LOGGER = LogManager.getLogger();

    private ChunkSectionUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ChunkSectionUtils. This is a utility class");
    }

    public static CompoundTag serializeNBT(final LevelChunkSection chunkSection) {
        final CompoundTag compressedSectionData = new CompoundTag();

        chunkSection.getStates().write(
          compressedSectionData,
          NbtConstants.PALETTE,
          NbtConstants.BLOCK_STATES
        );

        return compressedSectionData;
    }

    public static CompoundTag serializeNBTCompressed(final LevelChunkSection chunkSection) {
        final CompoundTag compressedSectionData = new CompoundTag();

        chunkSection.getStates().write(
          compressedSectionData,
          NbtConstants.PALETTE,
          NbtConstants.BLOCK_STATES
        );

        try
        {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            NbtIo.writeCompressed(compressedSectionData, outputStream);
            final byte[] compressedData = outputStream.toByteArray();
            final CompoundTag gzipCompressedTag = new CompoundTag();
            gzipCompressedTag.putBoolean(NbtConstants.DATA_IS_COMPRESSED, true);
            gzipCompressedTag.putByteArray(NbtConstants.COMPRESSED_DATA, compressedData);
            return gzipCompressedTag;
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to compress chiseled block data.", e);
            return compressedSectionData;
        }
    }

    public static void deserializeNBT(final LevelChunkSection chunkSection, final CompoundTag nbt) {
        if (nbt.isEmpty())
            return;

        if (nbt.contains(NbtConstants.DATA_IS_COMPRESSED, Tag.TAG_BYTE)
              && nbt.getBoolean(NbtConstants.DATA_IS_COMPRESSED)
              && nbt.contains(NbtConstants.COMPRESSED_DATA, Tag.TAG_BYTE_ARRAY)) {
            try
            {
                final byte[] compressedData = nbt.getByteArray(NbtConstants.COMPRESSED_DATA);
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
                final CompoundTag compoundTag = NbtIo.readCompressed(inputStream);

                chunkSection.getStates().read(
                  compoundTag.getList(NbtConstants.PALETTE, Constants.NBT.TAG_COMPOUND),
                  compoundTag.getLongArray(NbtConstants.BLOCK_STATES)
                );

                chunkSection.recalcBlockCounts();
                return;
            }
            catch (Exception e)
            {
                LOGGER.error("Failed to decompress chiseled block entity data. Resetting data.");
                ChunkSectionUtils.fillFromBottom(chunkSection, Blocks.AIR.defaultBlockState(), StateEntrySize.current().getBitsPerBlock());
                chunkSection.recalcBlockCounts();
                return;
            }
        }

        chunkSection.getStates().read(
          nbt.getList(NbtConstants.PALETTE, Constants.NBT.TAG_COMPOUND),
          nbt.getLongArray(NbtConstants.BLOCK_STATES)
        );

        chunkSection.recalcBlockCounts();
    }

    public static LevelChunkSection rotate90Degrees(final LevelChunkSection source, final Direction.Axis axis, final int rotationCount) {
        if (rotationCount == 0)
            return source;

        final Vec3 centerVector = new Vec3(7.5d, 7.5d, 7.5d);

        final LevelChunkSection target = new LevelChunkSection(0);

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

                    final BlockPos sourcePos = new BlockPos(workingVector);
                    final Vec3 offsetPos = rotatedVector.add(centerVector).multiply(1000,1000,1000);
                    final BlockPos targetPos = new BlockPos(new Vec3(Math.round(offsetPos.x()), Math.round(offsetPos.y()), Math.round(offsetPos.z())).multiply(1/1000d,1/1000d,1/1000d));

                    target.setBlockState(
                      targetPos.getX(),
                      targetPos.getY(),
                      targetPos.getZ(),
                      source.getBlockState(
                        sourcePos.getX(),
                        sourcePos.getY(),
                        sourcePos.getZ()
                      )
                    );
                }
            }
        }

        return target;
    }

    public static LevelChunkSection cloneSection(final LevelChunkSection lazyChunkSection)
    {
        final LevelChunkSection clone = new LevelChunkSection(0);
        deserializeNBT(clone, serializeNBT(lazyChunkSection));

        return clone;
    }

    public static void fillFromBottom(
      final LevelChunkSection chunkSection,
      final BlockState blockState,
      final int amount
    ) {
        final int loopCount = Math.max(0, Math.min(amount, StateEntrySize.current().getBitsPerBlock()));
        if (loopCount == 0)
            return;

        int count = 0;
        for (int y = 0; y < StateEntrySize.current().getBitsPerBlockSide(); y++)
        {
            for (int x = 0; x < StateEntrySize.current().getBitsPerBlockSide(); x++)
            {
                for (int z = 0; z < StateEntrySize.current().getBitsPerBlockSide(); z++)
                {
                    chunkSection.setBlockState(
                      x, y, z,
                      blockState
                    );

                    count++;
                    if (count == loopCount)
                        return;
                }
            }
        }
    }

    public static LevelChunkSection mirror(final LevelChunkSection lazyChunkSection, final Direction.Axis axis)
    {
        final LevelChunkSection result = new LevelChunkSection(0);

        for (int y = 0; y < StateEntrySize.current().getBitsPerBlockSide(); y++)
        {
            for (int x = 0; x < StateEntrySize.current().getBitsPerBlockSide(); x++)
            {
                for (int z = 0; z < StateEntrySize.current().getBitsPerBlockSide(); z++)
                {
                    final BlockState blockState = lazyChunkSection.getBlockState(x, y, z);

                    final int mirroredX = axis == Direction.Axis.X ? (StateEntrySize.current().getBitsPerBlockSide() - x - 1) : x;
                    final int mirroredY = axis == Direction.Axis.Y ? (StateEntrySize.current().getBitsPerBlockSide() - y - 1) : y;
                    final int mirroredZ = axis == Direction.Axis.Z ? (StateEntrySize.current().getBitsPerBlockSide() - z - 1) : z;

                    result.setBlockState(
                      mirroredX, mirroredY, mirroredZ,
                      blockState,
                      false
                    );
                }
            }
        }

        return result;
    }
}
