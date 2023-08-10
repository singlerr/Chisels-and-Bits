package mod.chiselsandbits.item.multistate;

import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public final class MultiStateItemStackManager {
    private static final MultiStateItemStackManager INSTANCE = new MultiStateItemStackManager();

    public static MultiStateItemStackManager getInstance() {
        return INSTANCE;
    }

    private final Map<ItemStack, IMultiStateItemStack> itemStackToMultiStateItemStack = Collections.synchronizedMap(new WeakHashMap<>());

    private MultiStateItemStackManager() {
    }

    public IMultiStateItemStack getManagedStack(final ItemStack itemStack, Function<ItemStack, IMultiStateItemStack> multiStateItemSupplier) {
        if (this.itemStackToMultiStateItemStack.containsKey(itemStack)) {
            return this.itemStackToMultiStateItemStack.get(itemStack);
        }

        final IMultiStateItemStack multiStateItemStack = multiStateItemSupplier.apply(itemStack);
        this.itemStackToMultiStateItemStack.put(itemStack, multiStateItemStack);
        return multiStateItemStack;
    }
}
