package mod.chiselsandbits.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import mod.chiselsandbits.api.serialization.CBCodecs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class StorageEngineBuilder<TPayload> {

    private final LinkedList<MapCodec<TPayload>> storageHandlers = new LinkedList<>();

    private int minimalVersion = 0;

    private StorageEngineBuilder() {
    }

    public static <T> StorageEngineBuilder<T> create() {
        return new StorageEngineBuilder<>();
    }

    public StorageEngineBuilder<TPayload> minimalVersion(final int version) {
        this.minimalVersion = version;
        return this;
    }

    public StorageEngineBuilder<TPayload> with(final MapCodec<TPayload> handler) {
        storageHandlers.add(handler);
        return this;
    }

    public Codec<TPayload> build() {
        final Map<Integer, MapCodec<TPayload>> versions = new HashMap<>();
        for (int i = 0; i < storageHandlers.size(); i++) {
            versions.put(i + minimalVersion, storageHandlers.get(i));
        }

        return CBCodecs.versioned(versions);
    }

    public IMultiThreadedStorageEngine<TPayload> buildMultiThreaded() {
        return new MultiThreadAwareStorageEngine<>(build());
    }
}
