package mod.chiselsandbits.item.bit;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;

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

    /**
     * Creates an itemstack that contains a bit of the given blockstate and is of the given size.
     *
     * @param blockState The given blockstate.
     * @param count      The amount of bits.
     * @return The itemstack with the given bits or empty if invalid.
     */
    @Override
    public ItemStack create(final BlockState blockState, final int count)
    {
        if (!IEligibilityManager.getInstance().canBeChiseled(blockState))
        {
            return ItemStack.EMPTY;
        }

        if (count <= 0 && count > getMaxStackSize())
        {
            return ItemStack.EMPTY;
        }

        final ItemStack resultStack = new ItemStack(ModItems.ITEM_BLOCK_BIT.get());

        resultStack.getOrCreateTag().put(NbtConstants.BLOCK_STATE, NBTUtil.writeBlockState(blockState));

        return resultStack;
    }
}
