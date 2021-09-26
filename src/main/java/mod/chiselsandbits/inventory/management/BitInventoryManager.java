package mod.chiselsandbits.inventory.management;

import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.bit.IBitInventoryItem;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.inventory.bit.IInventoryBitInventory;
import mod.chiselsandbits.inventory.bit.IItemHandlerBitInventory;
import mod.chiselsandbits.inventory.bit.IModifiableItemHandlerBitInventory;
import mod.chiselsandbits.inventory.bit.IllegalBitInventory;
import mod.chiselsandbits.inventory.player.PlayerMainAndOffhandInventoryWrapper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
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

    @Override
    public IBitInventory create(final Player playerEntity)
    {
        return this.create(new PlayerMainAndOffhandInventoryWrapper(playerEntity.getInventory()));
    }

    @Override
    public IBitInventory create(final IItemHandler itemHandler)
    {
        if (itemHandler instanceof IItemHandlerModifiable)
            return new IModifiableItemHandlerBitInventory((IItemHandlerModifiable) itemHandler);

        return new IItemHandlerBitInventory(itemHandler);
    }

    @Override
    public IBitInventory create(final Container inventory)
    {
        return new IInventoryBitInventory(inventory);
    }

    @Override
    public IBitInventory create(final ItemStack stack)
    {
        if (stack.getItem() instanceof IBitInventoryItem) {
            return ((IBitInventoryItem) stack.getItem()).create(stack);
        }

        return new IllegalBitInventory();
    }
}
