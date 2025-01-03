package mod.chiselsandbits.components.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.chiselsandbits.api.block.storage.StateEntryStorage;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.serialization.CBCodecs;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record MultiStateItemStackData(StateEntryStorage storage, Statistics statistics) {

    public static final Codec<MultiStateItemStackData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        StateEntryStorage.CODEC.fieldOf(NbtConstants.STORAGE).forGetter(MultiStateItemStackData::storage),
        Statistics.CODEC.fieldOf(NbtConstants.STATISTICS).forGetter(MultiStateItemStackData::statistics)
    ).apply(instance, MultiStateItemStackData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MultiStateItemStackData> STREAM_CODEC = StreamCodec.composite(
        StateEntryStorage.STREAM_CODEC,
        MultiStateItemStackData::storage,
        Statistics.STREAM_CODEC,
        MultiStateItemStackData::statistics,
        MultiStateItemStackData::new
    );

    public static MultiStateItemStackData empty() {
        return new MultiStateItemStackData(new StateEntryStorage(), new Statistics(BlockInformation.AIR, Collections.emptyMap()));
    }

    public MultiStateItemStackData(StateEntryStorage storage, Statistics statistics) {
        this.storage = storage.createSnapshot();
        this.statistics = statistics;
    }

    @Override
    public StateEntryStorage storage() {
        return storage.createSnapshot();
    }

    public record Statistics(BlockInformation primaryState, Map<BlockInformation, Integer> counts) {
        private static final Codec<Statistics> CODEC = RecordCodecBuilder.create(instance ->  instance.group(
                BlockInformation.CODEC.fieldOf(NbtConstants.PRIMARY_STATE).forGetter(Statistics::primaryState),
                CBCodecs.unboundedComplexMap(BlockInformation.CODEC, Codec.INT).fieldOf(NbtConstants.BLOCK_STATES).forGetter(Statistics::counts)
        ).apply(instance, Statistics::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, Statistics> STREAM_CODEC = StreamCodec.composite(
                BlockInformation.STREAM_CODEC,
                Statistics::primaryState,
                ByteBufCodecs.map(HashMap::new, BlockInformation.STREAM_CODEC, ByteBufCodecs.VAR_INT),
                Statistics::counts,
                Statistics::new
        );

        public Statistics(BlockInformation primaryState, Map<BlockInformation, Integer> counts) {
            this.counts = new HashMap<>(counts);
            this.primaryState = primaryState;
        }

        @Override
        public Map<BlockInformation, Integer> counts() {
            return Collections.unmodifiableMap(counts);
        }
    }
}
