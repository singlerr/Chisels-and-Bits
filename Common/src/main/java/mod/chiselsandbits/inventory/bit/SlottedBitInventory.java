package mod.chiselsandbits.inventory.bit;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mod.chiselsandbits.api.inventory.bit.watchable.IWatch;
import mod.chiselsandbits.api.inventory.bit.watchable.IWatchableBitInventory;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.util.INBTSerializable;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import mod.chiselsandbits.platforms.core.registries.IPlatformRegistryManager;
import mod.chiselsandbits.platforms.core.util.constants.NbtConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class SlottedBitInventory extends AbstractBitInventory implements IWatchableBitInventory, INBTSerializable<CompoundTag>
{
    protected final int size;
    protected final Int2ObjectMap<BitSlot> slotMap = new Int2ObjectArrayMap<>();

    private final Map<UUID, Runnable> onChangeCallbacks = Maps.newConcurrentMap();

    public SlottedBitInventory(
      final int size
    ) {
        this.size = size;
    }

    @Override
    protected ItemStack getItem(final int index)
    {
        final BitSlot bitSlot = slotMap.get(index);
        if (bitSlot == null)
            return ItemStack.EMPTY;

        return bitSlot.internalStack;
    }

    @Override
    protected int getInventorySize()
    {
        return size;
    }

    @Override
    protected void setSlotContents(final int index, final ItemStack stack)
    {
        if (stack.isEmpty())
        {
            slotMap.remove(index);
            onChange();
            return;
        }

        if (!(stack.getItem() instanceof final IBitItem bitItem))
            throw new IllegalArgumentException("Can not insert a none bit item into the inventory.");

        final BlockState state = bitItem.getBitState(stack);

        BitSlot slot = slotMap.get(index);
        if (slot == null)
            slot = new BitSlot();

        slot.setState(state);
        slot.setCount(stack.getCount());

        if (!slotMap.containsKey(index))
            slotMap.put(index, slot);

        onChange();
    }

    @Override
    public IWatch startWatching(final Runnable onChangeCallback)
    {
        final UUID id = UUID.randomUUID();
        this.onChangeCallbacks.put(id, onChangeCallback);
        return () -> this.onChangeCallbacks.remove(id);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag data = new CompoundTag();

        this.slotMap.forEach((index, slot) -> data.put(index.toString(), slot.serializeNBT()));

        return data;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.slotMap.clear();

        nbt.getAllKeys().forEach(indexRep -> {
            final int index = Integer.parseInt(indexRep);
            final BitSlot slot = new BitSlot();
            slot.deserializeNBT(nbt.getCompound(indexRep));

            this.slotMap.put(index, slot);
        });

        onChange();
    }

    protected Collection<BitSlot> getContents() {
        return this.slotMap.values();
    }

    protected void onChange() {
        this.onChangeCallbacks.values().forEach(Runnable::run);
    }

    @Override
    public boolean isEmpty()
    {
        return this.slotMap.isEmpty() || this.slotMap.values().stream().allMatch(slot -> slot.getCount() == 0);
    }

    protected static final class BitSlot implements INBTSerializable<CompoundTag>, IPacketBufferSerializable {

        private BlockState state = Blocks.AIR.defaultBlockState();
        private ItemStack internalStack = ItemStack.EMPTY;

        public BitSlot()
        {
        }

        public BitSlot(final BlockState state, final int count)
        {
            this.state = state;
            this.internalStack = IBitItemManager.getInstance().create(state, count);
        }

        @Override
        public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
        {
            packetBuffer.writeVarInt(IPlatformRegistryManager.getInstance().getBlockStateIdMap().getId(state));
            packetBuffer.writeVarInt(getCount());
        }

        @Override
        public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
        {
            state = IPlatformRegistryManager.getInstance().getBlockStateIdMap().byId(packetBuffer.readVarInt());

            final int count = packetBuffer.readVarInt();
            internalStack = IBitItemManager.getInstance().create(state, count);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag data = new CompoundTag();

            data.put(NbtConstants.BLOCK_STATE, NbtUtils.writeBlockState(state));
            data.putInt(NbtConstants.COUNT, getCount());

            return data;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            state = NbtUtils.readBlockState(nbt.getCompound(NbtConstants.BLOCK_STATE));
            final int count = nbt.getInt(NbtConstants.COUNT);
            internalStack = IBitItemManager.getInstance().create(state, count);
        }

        public BlockState getState()
        {
            return state;
        }

        public int getCount()
        {
            return internalStack.getCount();
        }

        public void setState(final BlockState state)
        {
            if (this.state == state)
                return;

            this.state = state;
            internalStack = IBitItemManager.getInstance().create(state, 1);
        }

        public void setCount(final int count)
        {
            this.internalStack.setCount(count);
        }
    }
}
