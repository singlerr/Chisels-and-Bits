package mod.chiselsandbits.api.item.bit;

import mod.chiselsandbits.api.item.click.IRightClickControllingItem;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

/**
 * Represents an item which is a single bit.
 */
public interface IBitItem extends IRightClickControllingItem
{

    /**
     * Returns the blockstate which is contained in a stack with the
     * given bit item.
     *
     * @param stack The stack which contains this bit item.
     *
     * @return The blockstate contained in this bit item.
     */
    BlockState getBitState(final ItemStack stack);
}
