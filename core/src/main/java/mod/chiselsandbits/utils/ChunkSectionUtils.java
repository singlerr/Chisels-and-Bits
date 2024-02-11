package mod.chiselsandbits.utils;

import com.mojang.serialization.Codec;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ChunkSectionUtils
{
    private static final     Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC =
      PalettedContainer.codecRW(Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState());
    private static final Logger LOGGER            = LogManager.getLogger();

    private ChunkSectionUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ChunkSectionUtils. This is a utility class");
    }

    public static Tag serializeNBT(final LevelChunkSection chunkSection) {
        return BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, chunkSection.getStates()).getOrThrow(false, LOGGER::error);
    }

    public static CompoundTag serializeNBTCompressed(final LevelChunkSection chunkSection) {
        final Tag outputTag = BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, chunkSection.getStates()).getOrThrow(false, LOGGER::error);

        if (!(outputTag instanceof CompoundTag compressedSectionData))
            throw new IllegalStateException("Serialized into incompatible tag type: " + outputTag.getType().getPrettyName());

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

    public static void deserializeNBT(final LevelChunkSection chunkSection, final Tag tag) {
        if (!(tag instanceof CompoundTag compoundTag))
            return;

        if (compoundTag.isEmpty())
            return;

        if (compoundTag.contains(NbtConstants.DATA_IS_COMPRESSED, Tag.TAG_BYTE)
              && compoundTag.getBoolean(NbtConstants.DATA_IS_COMPRESSED)
              && compoundTag.contains(NbtConstants.COMPRESSED_DATA, Tag.TAG_BYTE_ARRAY)) {
            try
            {
                final byte[] compressedData = compoundTag.getByteArray(NbtConstants.COMPRESSED_DATA);
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
                final CompoundTag decompressedCompoundTag = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());

                chunkSection.states = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, decompressedCompoundTag)
                  .promotePartial(LOGGER::error)
                  .getOrThrow(false, LOGGER::error);

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

        chunkSection.states = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, compoundTag)
          .promotePartial(LOGGER::error)
          .getOrThrow(false, LOGGER::error);

        chunkSection.recalcBlockCounts();
    }

    public static LevelChunkSection rotate90Degrees(final LevelChunkSection source, final Direction.Axis axis, final int rotationCount) {
        if (rotationCount == 0)
            return source;

        final Vec3 centerVector = new Vec3(7.5d, 7.5d, 7.5d);

        final LevelChunkSection target = new LevelChunkSection(source.getStates(), source.getBiomes());

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
                    final BlockPos targetPos = VectorUtils.toBlockPos(new Vec3(Math.round(offsetPos.x()), Math.round(offsetPos.y()), Math.round(offsetPos.z())).multiply(1/1000d,1/1000d,1/1000d));

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
        final LevelChunkSection clone = new LevelChunkSection(lazyChunkSection.getStates(), lazyChunkSection.getBiomes());
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
        final LevelChunkSection result = new LevelChunkSection(lazyChunkSection.getStates(), lazyChunkSection.getBiomes());

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
