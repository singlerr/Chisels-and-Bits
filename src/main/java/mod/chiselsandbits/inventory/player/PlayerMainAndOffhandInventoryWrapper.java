package mod.chiselsandbits.inventory.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerMainAndOffhandInventoryWrapper implements IInventory
{

    private final PlayerInventory playerInventory;

    public PlayerMainAndOffhandInventoryWrapper(final PlayerInventory playerInventory) {this.playerInventory = playerInventory;}

    @Override
    public int getSizeInventory()
    {
        return 9*4+1;
    }

    @Override
    public boolean isEmpty()
    {
        return playerInventory.isEmpty();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int index)
    {
        if (index == 36)
            index += 4;

        return playerInventory.getStackInSlot(index);
    }

    @NotNull
    @Override
    public ItemStack decrStackSize(int index, final int count)
    {
        if (index == 36)
            index += 4;

        return playerInventory.decrStackSize(index, count);
    }

    @NotNull
    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        if (index == 36)
            index += 4;

        return playerInventory.removeStackFromSlot(index);
    }

    @Override
    public void setInventorySlotContents(int index, @NotNull final ItemStack stack)
    {
        if (index == 36)
            index += 4;

        playerInventory.setInventorySlotContents(index, stack);
    }

    @Override
    public void markDirty()
    {
        playerInventory.markDirty();
    }

    @Override
    public boolean isUsableByPlayer(@NotNull final PlayerEntity player)
    {
        return playerInventory.isUsableByPlayer(player);
    }

    @Override
    public void clear()
    {
        playerInventory.clear();
    }
}
