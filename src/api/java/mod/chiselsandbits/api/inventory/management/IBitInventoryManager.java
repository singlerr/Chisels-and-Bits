package mod.chiselsandbits.api.inventory.management;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
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
    default IBitInventory create(final PlayerEntity playerEntity) {
        return this.create(playerEntity.inventory);
    }

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
    IBitInventory create(final IInventory inventory);

    /**
     * Creates a new bit inventory wrapping the given itemstack.
     *
     * @param stack The itemstack to wrap.
     *
     * @return The bit inventory which represents the inventory.
     */
    IBitInventory create(final ItemStack stack);
}
