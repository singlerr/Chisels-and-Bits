package mod.chiselsandbits.storage;

import java.util.LinkedList;
import java.util.concurrent.Executor;

public class StorageEngineBuilder {

    private final LinkedList<IStorageHandler<?>> storageHandlers = new LinkedList<>();

    private int minimalVersion = 0;

    private StorageEngineBuilder() {
    }

    public static StorageEngineBuilder create() {
        return new StorageEngineBuilder();
    }

    public StorageEngineBuilder minimalVersion(final int version) {
        this.minimalVersion = version;
        return this;
    }

    public StorageEngineBuilder with(final IStorageHandler<?> handler) {
        storageHandlers.add(handler);
        return this;
    }

    public IStorageEngine build() {
        return new VersionedStorageEngine(minimalVersion, storageHandlers);
    }

    public IThreadAwareStorageEngine buildThreadAware() {
        return new VersionedStorageEngine(minimalVersion, storageHandlers);
    }

    public IMultiThreadedStorageEngine buildMultiThreaded(final Executor gameExecutor) {
        return new MultiThreadAwareStorageEngine(buildThreadAware(), gameExecutor);
    }
}
