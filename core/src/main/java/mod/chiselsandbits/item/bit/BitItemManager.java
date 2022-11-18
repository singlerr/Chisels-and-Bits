package mod.chiselsandbits.item.bit;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.world.item.ItemStack;

public class BitItemManager implements IBitItemManager
{
    private static final BitItemManager INSTANCE = new BitItemManager();

    private BitItemManager()
    {
    }

    public static BitItemManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * The maximum amount of bits that fits into a single itemstack.
     *
     * @return The maximum amount of bits.
     */
    @SuppressWarnings("deprecation")
    @Override
    public int getMaxStackSize()
    {
        return ModItems.ITEM_BLOCK_BIT.get().getMaxStackSize();
    }

    @Override
    public ItemStack create(final BlockInformation blockInformation, final int count)
    {
        if (blockInformation == null || blockInformation.isAir())
        {
            return ItemStack.EMPTY;
        }

        if (!IEligibilityManager.getInstance().canBeChiseled(blockInformation))
        {
            return ItemStack.EMPTY;
        }

        if (count <= 0 && count > getMaxStackSize())
        {
            return ItemStack.EMPTY;
        }

        final ItemStack resultStack = new ItemStack(ModItems.ITEM_BLOCK_BIT.get());

        resultStack.getOrCreateTag().put(NbtConstants.BLOCK_INFORMATION, blockInformation.serializeNBT());
        resultStack.setCount(count);

        return resultStack;
    }
}
