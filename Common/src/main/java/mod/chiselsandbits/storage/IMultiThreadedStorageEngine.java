package mod.chiselsandbits.storage;

import net.minecraft.nbt.CompoundTag;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a multi-threaded storage engine, which can process data for IO purposes.
 */
public interface IMultiThreadedStorageEngine extends IStorageEngine
{
    /**
     * Runs the save process off-thread and returns the scheduled task.
     *
     * @return The off-thread save task.
     */
    CompletableFuture<CompoundTag> serializeOffThread();
}
