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
    public int getContainerSize()
    {
        return whenNotNull(
          wrapped,
          0,
          IInventory::getContainerSize
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
    public ItemStack getItem(final int index)
    {
        return whenNotNull(
          wrapped,
          ItemStack.EMPTY,
          inventory -> inventory.getItem(index)
        );
    }

    @NotNull
    @Override
    public ItemStack removeItem(final int index, final int count)
    {
        return whenNotNull(
          wrapped,
          ItemStack.EMPTY,
          inventory -> inventory.removeItem(index, count)
        );
    }

    @NotNull
    @Override
    public ItemStack removeItemNoUpdate(final int index)
    {
        return whenNotNull(
          wrapped,
          ItemStack.EMPTY,
          inventory -> inventory.removeItemNoUpdate(index)
        );
    }

    @Override
    public void setItem(final int index, final @NotNull ItemStack stack)
    {
        whenNotNull(
          wrapped,
          inventory -> inventory.setItem(index, stack)
        );
    }

    @Override
    public void setChanged()
    {
        whenNotNull(
          wrapped,
          IInventory::setChanged
        );
    }

    @Override
    public boolean stillValid(final @NotNull PlayerEntity player)
    {
        return whenNotNull(
          wrapped,
          false,
          inventory -> inventory.stillValid(player)
        );
    }

    @Override
    public void clearContent()
    {
        whenNotNull(
          wrapped,
          IClearable::clearContent
        );
    }

    @Override
    public int getMaxStackSize()
    {
        return whenNotNull(
          wrapped,
          64,
          IInventory::getMaxStackSize
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
