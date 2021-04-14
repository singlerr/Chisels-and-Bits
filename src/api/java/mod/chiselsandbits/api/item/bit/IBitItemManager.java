package mod.chiselsandbits.api.item.bit;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.injection.invoke.arg.ArgumentIndexOutOfBoundsException;

/**
 * A manager which deals with items for bits.
 */
public interface IBitItemManager
{

    static IBitItemManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getBitItemManager();
    }

    /**
     * The maximum amount of bits that fits into a single
     * itemstack.
     *
     * @return The maximum amount of bits.
     */
    int getMaxStackSize();

    /**
     * Creates an itemstack that contains a bit of the given blockstate and is of the given size.
     *
     * @param blockState The given blockstate.
     * @param count The amount of bits.
     * @return The itemstack with the given bits.
     */
    ItemStack create(final BlockState blockState, final int count);

    /**
     * Creates an itemstack that contains a bit of the given blockstate with a size of 1.
     *
     * @param blockState The given blockstate.
     * @return The itemstack with the given bit.
     */
    default ItemStack create(final BlockState blockState) {
        return this.create(blockState, 1);
    }
}
