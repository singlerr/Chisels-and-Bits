package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.container.BagContainer;
import mod.chiselsandbits.container.ChiseledPrinterContainer;
import mod.chiselsandbits.container.ModificationTableContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModContainerTypes
{


    private ModContainerTypes()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModContainers. This is a utility class");
    }

    private static final DeferredRegister<MenuType<?>> CONTAINER_TYPE_REGISTRAR = DeferredRegister.create(ForgeRegistries.CONTAINERS, Constants.MOD_ID);

    public static final RegistryObject<MenuType<BagContainer>> BIT_BAG = CONTAINER_TYPE_REGISTRAR.register(
      "bag",
      () -> new MenuType<>(BagContainer::new)
    );

    public static final RegistryObject<MenuType<ModificationTableContainer>> MODIFICATION_TABLE = CONTAINER_TYPE_REGISTRAR.register(
      "modification_table",
      () -> new MenuType<>(ModificationTableContainer::new)
    );

    public static final RegistryObject<MenuType<ChiseledPrinterContainer>> CHISELED_PRINTER_CONTAINER = CONTAINER_TYPE_REGISTRAR.register(
      "chiseled_printer",
      () -> new MenuType<>(ChiseledPrinterContainer::new)
    );

    public static void onModConstruction() {
        CONTAINER_TYPE_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
