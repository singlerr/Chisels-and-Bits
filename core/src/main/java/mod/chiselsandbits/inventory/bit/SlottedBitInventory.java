package mod.chiselsandbits.inventory.bit;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.inventory.bit.watchable.IWatch;
import mod.chiselsandbits.api.inventory.bit.watchable.IWatchableBitInventory;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.serialization.Serializable;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class SlottedBitInventory extends AbstractBitInventory implements IWatchableBitInventory
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

        return IChiselsAndBitsAPI.getInstance().getBitItemManager().create(
            bitSlot.getBlockInformation(),
            bitSlot.getCount()
        );
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

        final BlockInformation state = bitItem.getBlockInformation(stack);

        BitSlot slot = slotMap.get(index);
        if (slot == null)
            slot = new BitSlot();

        slot.setBlockInformation(state);
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

    protected static final class BitSlot {

        private BlockInformation blockInformation = BlockInformation.AIR;
        private int count;

        public BitSlot()
        {
        }

        public BitSlot(final BlockInformation blockInformation, final int count)
        {
            this.blockInformation = blockInformation;
            this.count = count;
        }

        public BlockInformation getBlockInformation()
        {
            return blockInformation;
        }

        public int getCount()
        {
            return count;
        }

        public void setBlockInformation(final BlockInformation blockInformation)
        {
            this.blockInformation = blockInformation;
        }

        public void setCount(final int count)
        {
            this.count = count;
        }
    }
}
