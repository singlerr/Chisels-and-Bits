package mod.chiselsandbits.bitbag;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotReadonly extends Slot
{

	public SlotReadonly(
			final IInventory inventoryIn,
			final int index,
			final int xPosition,
			final int yPosition )
	{
		super( inventoryIn, index, xPosition, yPosition );
	}

	@Override
	public boolean isItemValid(
			final ItemStack stack )
	{
		return false;
	}

	@Override
	public boolean canTakeStack(
			final PlayerEntity playerIn )
	{
		return false;
	}
}
