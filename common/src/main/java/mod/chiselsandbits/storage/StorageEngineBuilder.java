package mod.chiselsandbits.storage;

import java.util.LinkedList;
import java.util.concurrent.Executor;

public class StorageEngineBuilder
{

    private final LinkedList<ILegacyStorageHandler> legacyStorageHandlers = new LinkedList<>();
    private final LinkedList<IStorageHandler> storageHandlers = new LinkedList<>();

    private int minimalVersion = 0;

    public static StorageEngineBuilder create() {
        return new StorageEngineBuilder();
    }

    private StorageEngineBuilder()
    {
    }

    public StorageEngineBuilder withLegacy( final ILegacyStorageHandler handler ) {
        legacyStorageHandlers.add( handler );
        return this;
    }

    public StorageEngineBuilder minimalVersion( final int version ) {
        this.minimalVersion = version;
        return this;
    }

    public StorageEngineBuilder with( final IStorageHandler handler ) {
        storageHandlers.add( handler );
        return this;
    }

    public IThreadAwareStorageEngine build() {
        return new LegacyAwareStorageEngine(
          new LegacyVersionedStorageEngine(legacyStorageHandlers),
          new VersionedStorageEngine(minimalVersion, storageHandlers)
          );
    }

    public IMultiThreadedStorageEngine buildMultiThreaded(final Executor gameExecutor) {
        return new MultiThreadAwareStorageEngine(gameExecutor, build());
    }
}
