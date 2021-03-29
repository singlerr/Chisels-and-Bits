package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.util.constants.NbtConstants;
import net.minecraft.nbt.CompoundNBT;
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
    }
}
