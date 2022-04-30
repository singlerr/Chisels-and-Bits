package mod.chiselsandbits.forge.platform.inventory.bit;

import mod.chiselsandbits.platforms.core.inventory.bit.IAdaptingBitInventoryManager;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Optional;

public class ForgeAdaptingBitInventoryManager implements IAdaptingBitInventoryManager
{
    private static final ForgeAdaptingBitInventoryManager INSTANCE = new ForgeAdaptingBitInventoryManager();

    public static ForgeAdaptingBitInventoryManager getInstance()
    {
        return INSTANCE;
    }

    private ForgeAdaptingBitInventoryManager()
    {
    }

    @Override
    public Optional<Object> create(final Object target)
    {
        return Optional.of(target)
          .filter(IItemHandler.class::isInstance)
          .map(IItemHandler.class::cast)
          .map(itemHandler -> {
              if (itemHandler instanceof IItemHandlerModifiable)
                  return new IModifiableItemHandlerBitInventory((IItemHandlerModifiable) itemHandler);

              return new IItemHandlerBitInventory(itemHandler);
          });
    }
}
