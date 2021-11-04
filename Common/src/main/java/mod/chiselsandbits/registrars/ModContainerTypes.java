package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.container.BagContainer;
import mod.chiselsandbits.container.ChiseledPrinterContainer;
import mod.chiselsandbits.container.ModificationTableContainer;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistrar;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import net.minecraft.world.inventory.MenuType;

public final class ModContainerTypes
{


    private ModContainerTypes()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModContainers. This is a utility class");
    }

    private static final IRegistrar<MenuType<?>> CONTAINER_TYPE_REGISTRAR = IRegistrar.create(MenuType.class, Constants.MOD_ID);

    public static final IRegistryObject<MenuType<BagContainer>> BIT_BAG = CONTAINER_TYPE_REGISTRAR.register(
      "bag",
      () -> new MenuType<>(BagContainer::new)
    );

    public static final IRegistryObject<MenuType<ModificationTableContainer>> MODIFICATION_TABLE = CONTAINER_TYPE_REGISTRAR.register(
      "modification_table",
      () -> new MenuType<>(ModificationTableContainer::new)
    );

    public static final IRegistryObject<MenuType<ChiseledPrinterContainer>> CHISELED_PRINTER_CONTAINER = CONTAINER_TYPE_REGISTRAR.register(
      "chiseled_printer",
      () -> new MenuType<>(ChiseledPrinterContainer::new)
    );
}
