package mod.chiselsandbits.api.item.bit;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import net.minecraft.world.item.ItemStack;

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
     * Creates an itemstack that contains a bit of the given block information and is of the given size.
     *
     * @param blockInformation The given block information.
     * @param count The amount of bits.
     * @return The itemstack with the given bits.
     */
    ItemStack create(final IBlockInformation blockInformation, final int count);

    /**
     * Creates an itemstack that contains a bit of the given block information with a size of 1.
     *
     * @param blockInformation The given block information.
     * @return The itemstack with the given bit.
     */
    default ItemStack create(final IBlockInformation blockInformation) {
        return this.create(blockInformation, 1);
    }
}
