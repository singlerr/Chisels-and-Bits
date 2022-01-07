package mod.chiselsandbits.storage;

import java.util.LinkedList;

public class StorageEngineBuilder
{

    private final LinkedList<ILegacyStorageHandler> legacyStorageHandlers = new LinkedList<ILegacyStorageHandler>();
    private final LinkedList<IStorageHandler> storageHandlers = new LinkedList<IStorageHandler>();

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

    public IStorageEngine build() {
        return new LegacyAwareStorageEngine(
          new LegacyVersionedStorageEngine(legacyStorageHandlers),
          new VersionedStorageEngine(minimalVersion, storageHandlers)
          );
    }

    public IMultiThreadedStorageEngine buildMultiThreaded() {
        return new MultiThreadAwareStorageEngine(build());
    }
}
