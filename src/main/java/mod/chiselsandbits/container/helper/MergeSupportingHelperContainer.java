package mod.chiselsandbits.container.helper;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MergeSupportingHelperContainer extends AbstractContainerMenu
{
    public MergeSupportingHelperContainer()
    {
        super(null, 0);
    }

    @Override
    public boolean stillValid(final @NotNull Player playerIn)
    {
        return false;
    }

    public boolean doMergeItemStack(final ItemStack stack, final int startIndex, final int endIndex, final boolean reverseDirection)
    {
        return super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
    }
}
