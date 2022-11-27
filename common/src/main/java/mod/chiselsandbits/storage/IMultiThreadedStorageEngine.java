package mod.chiselsandbits.storage;

import net.minecraft.nbt.CompoundTag;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Represents a multi-threaded storage engine, which can process data for IO purposes.
 */
public interface IMultiThreadedStorageEngine extends IStorageEngine, Executor
{
    /**
     * Runs the save process off-thread and returns the scheduled task.
     *
     * @param resultSaver The builder which is able to produce a results processing task.
     * @return The off-thread save task.
     */
    CompletableFuture<Void> serializeOffThread(Function<CompoundTag, CompletableFuture<Void>> resultSaver);

    CompletableFuture<Void> deserializeOffThread(CompoundTag tag);
}
