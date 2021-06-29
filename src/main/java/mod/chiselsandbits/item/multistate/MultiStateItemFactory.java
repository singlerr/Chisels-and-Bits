package mod.chiselsandbits.item.multistate;

import mod.chiselsandbits.api.exceptions.StateEntryInfoIsToBigException;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemFactory;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.NotImplementedException;

public class MultiStateItemFactory implements IMultiStateItemFactory
{
    private static final MultiStateItemFactory INSTANCE = new MultiStateItemFactory();

    private MultiStateItemFactory()
    {
    }

    public static MultiStateItemFactory getInstance()
    {
        return INSTANCE;
    }

    /**
     * Creates a new multistate itemstack with a single state internally.
     *
     * @param stateEntryInfo The state entry info to create an itemstack for.
     * @return The itemstack containing only the given single state entry.
     */
    @SuppressWarnings("deprecation")
    @Override
    public ItemStack createBlockFrom(final IStateEntryInfo stateEntryInfo)
    {
        //TODO: Fix this in 1.17
        if (stateEntryInfo.getState().isAir(new SingleBlockBlockReader(stateEntryInfo.getState()), BlockPos.ZERO))
            return ItemStack.EMPTY;

        return IBitItemManager.getInstance().create(stateEntryInfo.getState());
    }
}
