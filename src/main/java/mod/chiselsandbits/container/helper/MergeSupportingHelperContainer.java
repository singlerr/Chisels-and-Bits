package mod.chiselsandbits.container.helper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MergeSupportingHelperContainer extends Container
{
    public MergeSupportingHelperContainer()
    {
        super(null, 0);
    }

    @Override
    public boolean stillValid(final @NotNull PlayerEntity playerIn)
    {
        return false;
    }

    public boolean doMergeItemStack(final ItemStack stack, final int startIndex, final int endIndex, final boolean reverseDirection)
    {
        return super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
    }
}
