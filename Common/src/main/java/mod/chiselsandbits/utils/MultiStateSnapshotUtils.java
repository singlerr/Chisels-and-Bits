package mod.chiselsandbits.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.multistate.snapshot.EmptySnapshot;
import mod.chiselsandbits.multistate.snapshot.LazilyDecodingSingleBlockMultiStateSnapshot;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MultiStateSnapshotUtils
{

    private static final Logger LOGGER = LogManager.getLogger();
    
    private MultiStateSnapshotUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: MultiStateSnapshotUtils. This is a utility class");
    }

    private static final Cache<BlockState, LevelChunkSection> FILLED_SECTION_CACHE = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
        .build();

    public static IMultiStateSnapshot createFromSection(final LevelChunkSection chunkSection) {
        return new LazilyDecodingSingleBlockMultiStateSnapshot(
          ChunkSectionUtils.serializeNBT(chunkSection)
        );
    }

    public static IMultiStateSnapshot createFilledWith(final BlockState blockState) {
        try
        {
            final LevelChunkSection chunkSection = FILLED_SECTION_CACHE.get(
              blockState,
              () -> {
                  final LevelChunkSection result = new LevelChunkSection(0, BuiltinRegistries.BIOME);
                  BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
                    .forEach(pos -> result.setBlockState(pos.getX(), pos.getY(), pos.getZ(), blockState));

                  return result;
              }
            );

            return createFromSection(chunkSection);
        }
        catch (ExecutionException e)
        {
            LOGGER.warn("Failed to get a filled snapshot for: " + blockState + ". The filling of the chunksection was aborted.", e);
            return EmptySnapshot.INSTANCE;
        }
    }
}
