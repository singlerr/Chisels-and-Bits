package mod.chiselsandbits.inventory.management;

import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.bit.IBitInventoryItem;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.inventory.bit.IInventoryBitInventory;
import mod.chiselsandbits.inventory.bit.IItemHandlerBitInventory;
import mod.chiselsandbits.inventory.bit.IModifiableItemHandlerBitInventory;
import mod.chiselsandbits.inventory.bit.IllegalBitInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class BitInventoryManager implements IBitInventoryManager
{
    private static final BitInventoryManager INSTANCE = new BitInventoryManager();

    private BitInventoryManager()
    {
    }

    public static BitInventoryManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Creates a new bit inventory wrapping the given {@link IItemHandler}.
     * <p>
     * This inventory is aware of items which themselves can act as a bit inventory.
     *
     * @param itemHandler The {@link IItemHandler}.
     * @return The bit inventory which represents the inventory.
     */
    @Override
    public IBitInventory create(final IItemHandler itemHandler)
    {
        if (itemHandler instanceof IItemHandlerModifiable)
            return new IModifiableItemHandlerBitInventory((IItemHandlerModifiable) itemHandler);

        return new IItemHandlerBitInventory(itemHandler);
    }

    /**
     * Creates a new bit inventory wrapping the given inventory.
     * <p>
     * This inventory is aware of items which themselves can act as a bit inventory.
     *
     * @param inventory The inventory.
     * @return The bit inventory which represents the inventory.
     */
    @Override
    public IBitInventory create(final IInventory inventory)
    {
        return new IInventoryBitInventory(inventory);
    }

    /**
     * Creates a new bit inventory wrapping the given itemstack.
     *
     * @param stack The itemstack to wrap.
     * @return The bit inventory which represents the inventory.
     */
    @Override
    public IBitInventory create(final ItemStack stack)
    {
        if (stack.getItem() instanceof IBitInventoryItem) {
            return ((IBitInventoryItem) stack.getItem()).create(stack);
        }

        return new IllegalBitInventory();
    }
}
