package mod.chiselsandbits.utils.container;

import com.google.common.collect.Lists;
import mod.chiselsandbits.api.util.INBTSerializable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleContainer implements Container, INBTSerializable<CompoundTag>
{
    private int                     size;
    private NonNullList<ItemStack>  items;
    private List<ContainerListener> listeners;

    public SimpleContainer(int size) {
        this.size = size;
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public SimpleContainer(ItemStack... content) {
        this.size = content.length;
        this.items = NonNullList.of(ItemStack.EMPTY, content);
    }

    public void addListener(ContainerListener param0) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }

        this.listeners.add(param0);
    }

    public void removeListener(ContainerListener param0) {
        this.listeners.remove(param0);
    }

    public @NotNull ItemStack getItem(int index) {
        return index >= 0 && index < this.items.size() ? this.items.get(index) : ItemStack.EMPTY;
    }

    public List<ItemStack> removeAllItems() {
        List<ItemStack> list = this.items.stream().filter((stack) -> !stack.isEmpty()).collect(Collectors.toList());
        this.clearContent();
        return list;
    }

    public @NotNull ItemStack removeItem(int index, int count) {
        ItemStack removeResult = ContainerHelper.removeItem(this.items, index, count);
        if (!removeResult.isEmpty()) {
            this.setChanged();
        }

        return removeResult;
    }

    public ItemStack removeItemType(Item item, int toExtract) {
        ItemStack nullStack = new ItemStack(item, 0);

        for(int reversedIndex = this.size - 1; reversedIndex >= 0; --reversedIndex) {
            ItemStack stack = this.getItem(reversedIndex);
            if (stack.getItem().equals(item)) {
                int remaining = toExtract - nullStack.getCount();
                ItemStack splitResult = stack.split(remaining);
                nullStack.grow(splitResult.getCount());
                if (nullStack.getCount() == toExtract) {
                    break;
                }
            }
        }

        if (!nullStack.isEmpty()) {
            this.setChanged();
        }

        return nullStack;
    }

    public ItemStack addItem(ItemStack input) {
        ItemStack var0 = input.copy();
        this.moveItemToOccupiedSlotsWithSameType(var0);
        if (var0.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.moveItemToEmptySlots(var0);
            return var0.isEmpty() ? ItemStack.EMPTY : var0;
        }
    }

    public boolean canAddItem(ItemStack input) {
        boolean foundEmptyOrMatching = false;

        for (final ItemStack stack : this.items)
        {
            if (stack.isEmpty() || ItemStack.isSameItemSameTags(stack, input) && stack.getCount() < stack.getMaxStackSize())
            {
                foundEmptyOrMatching = true;
                break;
            }
        }

        return foundEmptyOrMatching;
    }

    public @NotNull ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = this.items.get(index);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.items.set(index, ItemStack.EMPTY);
            return stack;
        }
    }

    public void setItem(int index, ItemStack stack) {
        this.items.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    public int getContainerSize() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    public void setChanged() {
        if (this.listeners != null) {
            this.listeners.forEach(listener -> listener.containerChanged(this));
        }
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    public void fillStackedContents(StackedContents stackedContents) {
        for (final ItemStack stack : this.items)
        {
            stackedContents.accountStack(stack);
        }
    }

    public String toString() {
        return this.items.stream().filter((stack) -> !stack.isEmpty()).collect(Collectors.toList()).toString();
    }

    private void moveItemToEmptySlots(ItemStack input) {
        for(int index = 0; index < this.size; ++index) {
            ItemStack stack = this.getItem(index);
            if (stack.isEmpty()) {
                this.setItem(index, input.copy());
                input.setCount(0);
                return;
            }
        }

    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack input) {
        for(int index = 0; index < this.size; ++index) {
            ItemStack stack = this.getItem(index);
            if (ItemStack.isSameItemSameTags(stack, input)) {
                this.moveItemsBetweenStacks(input, stack);
                if (input.isEmpty()) {
                    return;
                }
            }
        }

    }

    private void moveItemsBetweenStacks(ItemStack leftInput, ItemStack rightInput) {
        int maxTransfer = Math.min(this.getMaxStackSize(), rightInput.getMaxStackSize());
        int toTransfer = Math.min(leftInput.getCount(), maxTransfer - rightInput.getCount());
        if (toTransfer > 0) {
            rightInput.grow(toTransfer);
            leftInput.shrink(toTransfer);
            this.setChanged();
        }
    }

    public void fromTag(ListTag param0) {
        for(int var0 = 0; var0 < param0.size(); ++var0) {
            ItemStack var1 = ItemStack.of(param0.getCompound(var0));
            if (!var1.isEmpty()) {
                this.addItem(var1);
            }
        }

    }

    public ListTag createTag() {
        ListTag var0 = new ListTag();

        for(int var1 = 0; var1 < this.getContainerSize(); ++var1) {
            ItemStack var2 = this.getItem(var1);
            if (!var2.isEmpty()) {
                var0.add(var2.save(new CompoundTag()));
            }
        }

        return var0;
    }

    public void setSize(int size)
    {
        this.size = size;
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
        setChanged();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < items.size(); i++)
        {
            if (!items.get(i).isEmpty())
            {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                items.get(i).save(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", items.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : items.size());
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++)
        {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < items.size())
            {
                items.set(slot, ItemStack.of(itemTags));
            }
        }
        setChanged();
    }
}
