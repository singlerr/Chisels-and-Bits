package mod.chiselsandbits.storage;

import net.minecraft.nbt.CompoundTag;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Represents a single threaded storage engine which is aware of the fact that it can be invoked from off-thread, which can process data for IO purposes.
 */
public interface IThreadAwareStorageEngine extends IStorageEngine {

    /**
     * Runs the deserialization process off-thread and returns the scheduled task.
     *
     * @param tag The tag to deserialize.
     * @param ioExecutor The executor to use for IO operations.
     * @param gameExecutor The executor to use for game operations.
     * @return The scheduled tasks
     */
    CompletableFuture<Void> deserializeOffThread(CompoundTag tag, Executor ioExecutor, Executor gameExecutor);
}
