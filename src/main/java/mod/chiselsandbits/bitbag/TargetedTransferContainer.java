package mod.chiselsandbits.bitbag;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TargetedTransferContainer extends Container
{

    protected TargetedTransferContainer()
    {
        super(null, 0);
    }

    @Override
	public boolean canInteractWith(
      final PlayerEntity playerIn )
	{
		return true;
	}

	public boolean doMergeItemStack(
			final ItemStack stack,
			final int startIndex,
			final int endIndex,
			final boolean reverseDirection )
	{
		return mergeItemStack( stack, startIndex, endIndex, reverseDirection );
	}

}
