package mod.chiselsandbits.api.serialization;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface CBCodecs {
    Codec<BitSet> BIT_SET = Codec.LONG_STREAM
            .xmap(LongStream::toArray, LongStream::of)
            .xmap(BitSet::valueOf, BitSet::toLongArray);

    /**
     * Creates a codec that is versioned.
     * The version is stored as an integer in the payload.
     *
     * @param versions The versions to use.
     * @return The codec.
     * @param <T> The type of the codec.
     */
    static <T> Codec<T> versioned(Map<Integer, MapCodec<T>> versions) {
        final int maxVersion = versions.keySet().stream().max(Integer::compareTo).orElse(0);
        return Codec.INT.dispatch(NbtConstants.VERSION,
                t -> maxVersion,
                key -> {
                    if (!versions.containsKey(key)) {
                        throw new IllegalStateException("Unknown version: " + key);
                    }
                    return versions.get(key);
                });
    }

    /**
     * Creates a codec that compresses the data using LZ4.
     * The compressed data is stored as a byte array in the payload.
     *
     * @param inner The inner codec.
     */
    static <T> Codec<T> compressed(Codec<T> inner) {
        return Codec.BYTE_BUFFER.flatXmap(
                byteBuffer -> {
                    final byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);

                    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                    try {
                        final LZ4FrameInputStream lz4FrameInputStream = new LZ4FrameInputStream(byteArrayInputStream);
                        final DataInput dataInput = new DataInputStream(lz4FrameInputStream);
                        final Tag uncompressedData = NbtIo.readAnyTag(dataInput, NbtAccounter.unlimitedHeap());

                        final NbtOps ops = NbtOps.INSTANCE;
                        final Dynamic<Tag> dynamic = new Dynamic<>(ops, uncompressedData);
                        return inner.parse(dynamic);
                    } catch (IOException e) {
                        return DataResult.error(() -> "Failed to decompress data: " + e.getMessage());
                    }
                },
                t -> {
                    final NbtOps ops = NbtOps.INSTANCE;
                    final Tag tag = inner.encodeStart(ops, t).getOrThrow((s) -> new IllegalStateException("Failed to encode data: " + s));

                    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    try
                    {
                        final OutputStream lz4Stream = new LZ4FrameOutputStream(outputStream);
                        final DataOutput dataOutput = new DataOutputStream(lz4Stream);
                        NbtIo.writeAnyTag(tag, dataOutput);
                        lz4Stream.close();

                        final byte[] compressedData = outputStream.toByteArray();
                        return DataResult.success(ByteBuffer.wrap(compressedData));
                    }
                    catch (IOException e)
                    {
                        return DataResult.error(() -> "Failed to compress data: " + e.getMessage());
                    }
                }
        );
    }

    static <R, C, V> Codec<Table<R, C, V>> unboundedTable(Codec<R> row, Codec<C> column, Codec<V> value) {
        return unboundedComplexMap(
                row,
                unboundedComplexMap(
                        column,
                        value
                )
        ).xmap(
                rMapMap -> {
                    final Table<R, C, V> table = HashBasedTable.create();
                    rMapMap.forEach((r, cMap) -> cMap.forEach((c, v) -> table.put(r, c, v)));
                    return table;
                },
                Table::rowMap
        );
    }

    static <T> Codec<T> lazyNbtAware(Codec<T> codec, Supplier<? extends Tag> lazyTagSupplier) {
        return new Codec<T>() {
            @Override
            public <TData> DataResult<Pair<T, TData>> decode(DynamicOps<TData> ops, TData input) {
                return codec.decode(ops, input);
            }

            @Override
            public <TData> DataResult<TData> encode(T input, DynamicOps<TData> ops, TData prefix) {
                final Tag lazyTag = lazyTagSupplier.get();
                final NbtOps nbtOps = NbtOps.INSTANCE;

                if (lazyTag != null) {
                    return DataResult.success(nbtOps.convertTo(ops, lazyTag));
                }

                return codec.encode(input, ops, prefix);
            }
        };
    }

    static <T> MapCodec<T> lazyNbtAware(MapCodec<T> codec, Function<T, CompoundTag> lazyTagSupplier) {
        return new MapCodec<T>() {
            @Override
            public <T1> Stream<T1> keys(DynamicOps<T1> ops) {
                return codec.keys(ops);
            }

            @Override
            public <TData> DataResult<T> decode(DynamicOps<TData> ops, MapLike<TData> input) {
                return codec.decode(ops, input);
            }

            @Override
            public <TData> RecordBuilder<TData> encode(T input, DynamicOps<TData> ops, RecordBuilder<TData> prefix) {
                final CompoundTag lazyTag = lazyTagSupplier.apply(input);
                final NbtOps nbtOps = NbtOps.INSTANCE;

                if (lazyTag != null) {
                    lazyTag.getAllKeys().forEach(key -> {
                        final Tag tag = lazyTag.get(key);
                        if (tag != null) {
                            prefix.add(key, nbtOps.convertTo(ops, tag));
                        }
                    });

                    return prefix;
                }

                return codec.encode(input, ops, prefix);
            }
        };
    }

    static <K, V> Codec<Map<K, V>> unboundedComplexMap(Codec<K> keyCodec, Codec<V> valueCodec) {
        Validate.notNull(keyCodec, "Key codec cannot be null");
        Validate.notNull(valueCodec, "Value codec cannot be null");

        return RecordCodecBuilder.<Map.Entry<K, V>>create(instance ->
            instance.group(
                    keyCodec.fieldOf("key").forGetter(Map.Entry::getKey),
                    valueCodec.fieldOf("value").forGetter(Map.Entry::getValue)
            ).apply(instance, Map::entry)
        ).listOf().xmap(
                entries -> {
                    final Map<K, V> map = new HashMap<>();
                    entries.forEach(entry -> map.put(entry.getKey(), entry.getValue()));
                    return map;
                },
                map -> map.entrySet().stream().map(entry -> Map.entry(entry.getKey(), entry.getValue())).toList()
        );
    }
}
