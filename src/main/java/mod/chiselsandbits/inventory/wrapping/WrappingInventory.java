package mod.chiselsandbits.inventory.wrapping;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IClearable;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static mod.chiselsandbits.utils.NullUtils.whenNotNull;

public class WrappingInventory implements IInventory
{
    @Nullable
    private IInventory wrapped = null;

    @Override
    public int getSizeInventory()
    {
        return whenNotNull(
          wrapped,
          0,
          IInventory::getSizeInventory
        );
    }

    @Override
    public boolean isEmpty()
    {
        return whenNotNull(
          wrapped,
          true,
          IInventory::isEmpty
        );
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(final int index)
    {
        return whenNotNull(
          wrapped,
          ItemStack.EMPTY,
          inventory -> inventory.getStackInSlot(index)
        );
    }

    @NotNull
    @Override
    public ItemStack decrStackSize(final int index, final int count)
    {
        return whenNotNull(
          wrapped,
          ItemStack.EMPTY,
          inventory -> inventory.decrStackSize(index, count)
        );
    }

    @NotNull
    @Override
    public ItemStack removeStackFromSlot(final int index)
    {
        return whenNotNull(
          wrapped,
          ItemStack.EMPTY,
          inventory -> inventory.removeStackFromSlot(index)
        );
    }

    @Override
    public void setInventorySlotContents(final int index, final @NotNull ItemStack stack)
    {
        whenNotNull(
          wrapped,
          inventory -> inventory.setInventorySlotContents(index, stack)
        );
    }

    @Override
    public void markDirty()
    {
        whenNotNull(
          wrapped,
          IInventory::markDirty
        );
    }

    @Override
    public boolean isUsableByPlayer(final @NotNull PlayerEntity player)
    {
        return whenNotNull(
          wrapped,
          false,
          inventory -> inventory.isUsableByPlayer(player)
        );
    }

    @Override
    public void clear()
    {
        whenNotNull(
          wrapped,
          IClearable::clear
        );
    }

    @Override
    public int getInventoryStackLimit()
    {
        return whenNotNull(
          wrapped,
          64,
          IInventory::getInventoryStackLimit
        );
    }

    @Nullable
    public IInventory getWrapped()
    {
        return wrapped;
    }

    public void setWrapped(@Nullable final IInventory wrapped)
    {
        this.wrapped = wrapped;
    }


}
