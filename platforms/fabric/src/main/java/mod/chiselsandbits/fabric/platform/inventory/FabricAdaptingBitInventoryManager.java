package mod.chiselsandbits.fabric.platform.inventory;

import mod.chiselsandbits.platforms.core.inventory.bit.IAdaptingBitInventoryManager;

import java.util.Optional;

public final class FabricAdaptingBitInventoryManager implements IAdaptingBitInventoryManager
{
    private static final FabricAdaptingBitInventoryManager INSTANCE = new FabricAdaptingBitInventoryManager();

    public static FabricAdaptingBitInventoryManager getInstance()
    {
        return INSTANCE;
    }

    private FabricAdaptingBitInventoryManager()
    {
    }

    @Override
    public Optional<Object> create(final Object target)
    {
        return Optional.empty();
    }
}
