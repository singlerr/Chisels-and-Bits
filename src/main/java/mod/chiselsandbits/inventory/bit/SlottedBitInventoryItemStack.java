package mod.chiselsandbits.inventory.bit;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.inventory.bit.IBitInventoryItemStack;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SlottedBitInventoryItemStack extends SlottedBitInventory implements IBitInventoryItemStack
{
    private final Function<CompoundNBT, ItemStack> saveBuilder;

    public SlottedBitInventoryItemStack(final int size, final Function<CompoundNBT, ItemStack> saveBuilder)
    {
        super(size);

        this.saveBuilder = saveBuilder;
    }

    @Override
    public ItemStack toItemStack()
    {
        final CompoundNBT compoundNBT = this.serializeNBT();
        return this.saveBuilder.apply(compoundNBT);
    }

    @Override
    public List<ITextComponent> listContents()
    {
        return getContents().stream()
          .sorted(Comparator.comparingInt(BitSlot::getCount).reversed())
          .map(slot -> new TranslationTextComponent("chiselsandbits.bitbag.contents.enum.entry", slot.getCount(), slot.getState().getBlock().getTranslatedName()))
          .collect(Collectors.toList());
    }

    @Override
    public double getFilledRatio()
    {
        return this.slotMap.keySet().size() / (double) this.size;
    }

    @Override
    public void clear(final BlockState state)
    {
        final Int2ObjectMap<BitSlot> slots = new Int2ObjectArrayMap<>(this.slotMap);

        this.slotMap.clear();
        int slotIndex = 0;
        for (BitSlot bitSlot : slots.values())
        {
            if (bitSlot.getState() != state) {
                this.slotMap.put(slotIndex, bitSlot);
                slotIndex++;
            }
        }
    }

    @Override
    public void sort()
    {
        final Map<BlockState, Integer> contentMap = Maps.newHashMap();
        this.slotMap.values().forEach(bitSlot -> {
            contentMap.putIfAbsent(bitSlot.getState(), 0);
            contentMap.compute(bitSlot.getState(), (s, c) -> (c == null ? 0 : c) + bitSlot.getCount());
          }
        );

        this.slotMap.clear();

        List<Map.Entry<BlockState, Integer>> toSort = new ArrayList<>(contentMap.entrySet());
        toSort.sort(Map.Entry.<BlockState, Integer>comparingByValue().reversed());

        int slotIndex = 0;
        for (Map.Entry<BlockState, Integer> e : toSort)
        {
            int count = e.getValue();
            if (count == 0)
                continue;

            while (count > Configuration.getInstance().getServer().bagStackSize.get() && count > 0) {
                this.slotMap.put(slotIndex, new BitSlot(e.getKey(), Configuration.getInstance().getServer().bagStackSize.get()));
                slotIndex++;
                count -= Configuration.getInstance().getServer().bagStackSize.get();
            }

            if (count > 0) {
                this.slotMap.put(slotIndex, new BitSlot(e.getKey(), count));
                slotIndex++;
            }
        }
    }

    @Override
    public int getSizeInventory()
    {
        return this.size;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(final int index)
    {
        return super.getStackInSlot(index);
    }

    @Override
    public @NotNull ItemStack decrStackSize(final int index, final int count)
    {
        if (!this.slotMap.containsKey(index))
            return ItemStack.EMPTY;

        final BitSlot bitSlot = this.slotMap.get(index);
        final int containedCount = bitSlot.getCount();
        bitSlot.setCount(containedCount - count);
        if (bitSlot.getCount() <= 0)
            this.slotMap.remove(index);

        return IBitItemManager.getInstance().create(bitSlot.getState(), Math.min(containedCount, count));
    }

    @Override
    public @NotNull ItemStack removeStackFromSlot(final int index)
    {
        return decrStackSize(index, Integer.MAX_VALUE);
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack)
    {
        if (stack.isEmpty()) {
            this.slotMap.remove(index);
            return;
        }

        if (!(stack.getItem() instanceof IBitItem)) {
            return;
        }

        final IBitItem bitItem = (IBitItem) stack.getItem();
        final BlockState state = bitItem.getBitState(stack);

        final BitSlot bitSlot = this.slotMap.getOrDefault(index, new BitSlot());
        bitSlot.setState(state);
        bitSlot.setCount(stack.getCount());

        this.slotMap.put(index, bitSlot);
    }

    @Override
    public void markDirty()
    {
        onChange();
    }

    @Override
    public boolean isUsableByPlayer(final @NotNull PlayerEntity player)
    {
        return true;
    }

    @Override
    public void clear()
    {
        this.slotMap.clear();
        onChange();
    }

    @Override
    protected void onChange()
    {
        super.onChange();
        //We invoke to itemStack for sure to guarantee the saving.
        toItemStack();
    }

    @Override
    protected int getMaxBitsForSlot()
    {
        return Configuration.getInstance().getServer().bagStackSize.get();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return getMaxBitsForSlot();
    }

    //The following methods are needed to handle the obfuscation tree.
    @Override
    public boolean isEmpty()
    {
        return super.isEmpty();
    }
}
