package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.container.BagContainer;
import mod.chiselsandbits.container.ModificationTableContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModContainerTypes
{


    private ModContainerTypes()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModContainers. This is a utility class");
    }

    private static final DeferredRegister<ContainerType<?>> CONTAINER_TYPE_REGISTRAR = DeferredRegister.create(ForgeRegistries.CONTAINERS, Constants.MOD_ID);

    public static final RegistryObject<ContainerType<BagContainer>> BIT_BAG = CONTAINER_TYPE_REGISTRAR.register(
      "bag",
      () -> new ContainerType<>(BagContainer::new)
    );

    public static final RegistryObject<ContainerType<ModificationTableContainer>> MODIFICATION_TABLE = CONTAINER_TYPE_REGISTRAR.register(
      "modification_table",
      () -> new ContainerType<>(ModificationTableContainer::new)
    );

    public static void onModConstruction() {
        CONTAINER_TYPE_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
