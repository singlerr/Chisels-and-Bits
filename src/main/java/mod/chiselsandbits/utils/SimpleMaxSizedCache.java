package mod.chiselsandbits.utils;

import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class SimpleMaxSizedCache<K, V>
{

    private final LinkedHashMap<K, V> cache = new LinkedHashMap<>();

    private final LongSupplier maxSizeSupplier;

    public SimpleMaxSizedCache(final long maxSize)
    {
        Validate.exclusiveBetween(0, 10000000000L, maxSize);
        this.maxSizeSupplier = () -> maxSize;
    }

    public SimpleMaxSizedCache(final LongSupplier longSupplier) {
        this.maxSizeSupplier = longSupplier;
    }

    public SimpleMaxSizedCache(final IntSupplier intSupplier) {
        this.maxSizeSupplier = intSupplier::getAsInt;
    }

    private void evictFromCacheIfNeeded() {
        if (cache.size() == maxSizeSupplier.getAsLong()) {
            cache.remove(cache.keySet().iterator().next());
        }
    }

    public synchronized V get(final K key) {
        return cache.get(key);
    }

    public synchronized V get(final K key, final Supplier<V> valueSupplier) {
        if (!cache.containsKey(key))
            evictFromCacheIfNeeded();

        return cache.computeIfAbsent(key, (k) -> valueSupplier.get());
    }

    public synchronized Optional<V> getIfPresent(final K key) {
        return Optional.ofNullable(get(key));
    }

    public synchronized void put(final K key, final V value) {
        if (!cache.containsKey(key))
            evictFromCacheIfNeeded();

        cache.put(key, value);
    }

    public synchronized void clear() {
        this.cache.clear();
    }
}