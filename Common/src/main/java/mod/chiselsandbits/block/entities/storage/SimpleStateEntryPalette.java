package mod.chiselsandbits.block.entities.storage;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.mojang.serialization.DataResult;
import mod.chiselsandbits.api.util.INBTSerializable;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import mod.chiselsandbits.utils.BlockStateSerializationUtils;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

public class SimpleStateEntryPalette implements IPacketBufferSerializable, INBTSerializable<ListTag>
{

    private final List<Entry> paletteEntries = Lists.newArrayList();
    private final BiMap<BlockState, Entry> paletteMap = HashBiMap.create();
    private final IntConsumer onNewSizeAddedConsumer;

    public SimpleStateEntryPalette(final IntConsumer onNewSizeAddedConsumer) {
        this.onNewSizeAddedConsumer = onNewSizeAddedConsumer;
        clear(); //Reset to initial state
    }

    public SimpleStateEntryPalette(final IntConsumer onPaletteResize, final SimpleStateEntryPalette palette)
    {
        this.onNewSizeAddedConsumer = onPaletteResize;
        this.paletteEntries.addAll(palette.paletteEntries);
        this.paletteMap.putAll(palette.paletteMap);
    }

    @Override
    public ListTag serializeNBT()
    {
        return paletteEntries.stream().map(INBTSerializable::serializeNBT).collect(Collectors.toCollection(ListTag::new));
    }

    @Override
    public void deserializeNBT(final ListTag nbt)
    {
        final int currentSize = this.paletteEntries.size();
        this.paletteEntries.clear();

        nbt.stream()
          .filter(StringTag.class::isInstance)
          .map(StringTag.class::cast)
          .map(Entry::new)
          .forEach(this.paletteEntries::add);

        this.paletteEntries.forEach(entry -> this.paletteMap.put(entry.get(), entry));

        if (paletteEntries.size() == 0) {
            clear();
        }

        if (currentSize != this.paletteEntries.size()) {
            this.onNewSizeAddedConsumer.accept(this.paletteEntries.size());
        }
    }

    @Override
    public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeVarInt(this.paletteEntries.size());
        this.paletteEntries.forEach(entry -> entry.serializeInto(packetBuffer));
    }

    @Override
    public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
    {
        final int currentSize = this.paletteEntries.size();

        this.paletteEntries.clear();
        this.paletteMap.clear();

        final int newCount = packetBuffer.readVarInt();
        for (int i = 0; i < newCount; i++)
        {
            this.paletteEntries.add(new Entry(packetBuffer));
        }

        this.paletteEntries.forEach(entry -> this.paletteMap.put(entry.get(), entry));

        if (paletteEntries.size() == 0) {
            clear();
        }

        if (currentSize != this.paletteEntries.size()) {
            this.onNewSizeAddedConsumer.accept(this.paletteEntries.size());
        }
    }

    public int getIndex(final BlockState state) {
        if (this.paletteMap.containsKey(state)) {
            final Entry entry = this.paletteMap.get(state);
            return this.paletteEntries.indexOf(entry);
        }

        final Entry newEntry = new Entry(state);
        this.paletteMap.put(state, newEntry);

        this.paletteEntries.add(newEntry);
        this.onNewSizeAddedConsumer.accept(this.paletteEntries.size());

        return this.paletteEntries.size() - 1;
    }

    public BlockState getBlockState(final int blockStateId)
    {
        if (blockStateId < 0 || blockStateId >= this.paletteEntries.size())
            return getBlockState(0);

        return this.paletteEntries.get(blockStateId).get();
    }

    public void sanitize(final Collection<BlockState> toRemove) {
        final List<Entry> toRemoveList = toRemove.stream().map(this.paletteMap::get).toList();

        this.paletteEntries.removeAll(toRemoveList);
        toRemove.forEach(this.paletteMap::remove);

        this.onNewSizeAddedConsumer.accept(this.paletteEntries.size());
    }

    public void clear() {
        this.paletteEntries.clear();
        this.paletteMap.clear();
        this.getIndex(Blocks.AIR.defaultBlockState());
    }

    public List<BlockState> getStates()
    {
        return this.paletteMap.keySet().stream().toList();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof final SimpleStateEntryPalette that))
        {
            return false;
        }

        return paletteEntries.equals(that.paletteEntries);
    }

    @Override
    public int hashCode()
    {
        return paletteEntries.hashCode();
    }

    @Override
    public String toString()
    {
        return "SimpleStateEntryPalette{" +
                 "paletteEntries=" + paletteEntries +
                 '}';
    }

    private static final class Entry implements IPacketBufferSerializable, INBTSerializable<StringTag>
    {
        private BlockState outwardFacingState;
        private String rawSpec;

        private Entry(final BlockState newState) {
            this.outwardFacingState = newState;
            this.rawSpec = BlockStateSerializationUtils.serialize(newState);
        }

        private Entry(final StringTag tag) {
            deserializeNBT(tag);
        }

        private Entry(final FriendlyByteBuf buffer) {
            deserializeFrom(buffer);
        }

        @Override
        public StringTag serializeNBT()
        {
            return StringTag.valueOf(rawSpec);
        }

        @Override
        public void deserializeNBT(final StringTag nbt)
        {
            this.rawSpec = nbt.getAsString();
            final DataResult<BlockState> result = BlockStateSerializationUtils.deserialize(this.rawSpec);
            this.outwardFacingState = result.result().orElseGet(Blocks.AIR::defaultBlockState);
        }

        @Override
        public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
        {
            packetBuffer.writeUtf(this.rawSpec);
        }

        @Override
        public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
        {
            this.rawSpec = packetBuffer.readUtf();
            final DataResult<BlockState> result = BlockStateSerializationUtils.deserialize(this.rawSpec);
            this.outwardFacingState = result.result().orElseGet(Blocks.AIR::defaultBlockState);
        }

        public BlockState get()
        {
            return outwardFacingState;
        }
    }
}
