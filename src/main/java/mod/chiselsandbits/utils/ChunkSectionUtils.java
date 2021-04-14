package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.common.util.Constants;

public class ChunkSectionUtils
{

    private ChunkSectionUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ChunkSectionUtils. This is a utility class");
    }

    public static CompoundNBT serializeNBT(final ChunkSection chunkSection) {
        final CompoundNBT compressedSectionData = new CompoundNBT();

        chunkSection.getData().writeChunkPalette(
          compressedSectionData,
          NbtConstants.PALETTE,
          NbtConstants.BLOCK_STATES
        );

        return compressedSectionData;
    }

    public static void deserializeNBT(final ChunkSection chunkSection, final CompoundNBT nbt) {
        chunkSection.getData().readChunkPalette(
          nbt.getList(NbtConstants.PALETTE, Constants.NBT.TAG_COMPOUND),
          nbt.getLongArray(NbtConstants.BLOCK_STATES)
        );

        chunkSection.recalculateRefCounts();
    }

    public static ChunkSection rotate90Degrees(final ChunkSection source, final Direction.Axis axis, final int rotationCount) {
        if (rotationCount == 0)
            return source;

        final Vector3d centerVector = new Vector3d(7.5d, 7.5d, 7.5d);

        final ChunkSection target = new ChunkSection(0);

        for (int x = 0; x < 16; x++)
        {
            for (int y = 0; y < 16; y++)
            {
                for (int z = 0; z < 16; z++)
                {
                    final Vector3d workingVector = new Vector3d(x, y, z);
                    Vector3d rotatedVector = workingVector.subtract(centerVector);
                    for (int i = 0; i < rotationCount; i++)
                    {
                        rotatedVector = VectorUtils.rotate90Degrees(rotatedVector, axis);
                    }

                    final BlockPos sourcePos = new BlockPos(workingVector);
                    final BlockPos targetPos = new BlockPos(rotatedVector);

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

    public static ChunkSection cloneSection(final ChunkSection lazyChunkSection)
    {
        final ChunkSection clone = new ChunkSection(0);
        deserializeNBT(clone, serializeNBT(lazyChunkSection));

        return clone;
    }
}
