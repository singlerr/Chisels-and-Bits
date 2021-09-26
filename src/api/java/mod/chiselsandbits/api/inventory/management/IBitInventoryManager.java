package mod.chiselsandbits.api.inventory.management;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * Manager which deals with bit inventories.
 */
public interface IBitInventoryManager
{

    static IBitInventoryManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getBitInventoryManager();
    }

    /**
     * Creates a new bit inventory wrapping the inventory of the player.
     *
     * This player inventory is aware of items which themselves can act
     * as a bit inventory.
     *
     * @param playerEntity The player inventory.
     * @return The bit inventory which represents the players inventory.
     */
    IBitInventory create(final Player playerEntity);

    /**
     * Creates a new bit inventory wrapping the given {@link IItemHandler}.
     *
     * This inventory is aware of items which themselves can act
     * as a bit inventory.
     *
     * @param itemHandler The {@link IItemHandler}.
     * @return The bit inventory which represents the inventory.
     */
    IBitInventory create(final IItemHandler itemHandler);

    /**
     * Creates a new bit inventory wrapping the given inventory.
     *
     * This inventory is aware of items which themselves can act
     * as a bit inventory.
     *
     * @param inventory The inventory.
     * @return The bit inventory which represents the inventory.
     */
    IBitInventory create(final Container inventory);

    /**
     * Creates a new bit inventory wrapping the given itemstack.
     *
     * @param stack The itemstack to wrap.
     *
     * @return The bit inventory which represents the inventory.
     */
    IBitInventory create(final ItemStack stack);
}
