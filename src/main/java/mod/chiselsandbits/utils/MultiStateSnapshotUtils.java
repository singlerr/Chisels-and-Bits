package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.multistate.snapshot.LazilyDecodingSingleBlockMultiStateSnapshot;
import net.minecraft.world.chunk.ChunkSection;

public class MultiStateSnapshotUtils
{

    private MultiStateSnapshotUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: MultiStateSnapshotUtils. This is a utility class");
    }


    public static IMultiStateSnapshot createFromSection(final ChunkSection chunkSection) {
        return new LazilyDecodingSingleBlockMultiStateSnapshot(
          ChunkSectionUtils.serializeNBT(chunkSection)
        );
    }
}
