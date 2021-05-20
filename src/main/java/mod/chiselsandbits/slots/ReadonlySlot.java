package mod.chiselsandbits.slots;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ReadonlySlot extends Slot
{

    public ReadonlySlot(
      final IInventory inventoryIn,
      final int index,
      final int xPosition,
      final int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(
      final @NotNull ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean canTakeStack(
      final @NotNull PlayerEntity playerIn)
    {
        return false;
    }
}
