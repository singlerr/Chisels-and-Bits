package mod.chiselsandbits.registry;

import mod.chiselsandbits.bitbag.BagContainer;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.station.ChiselStationContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModContainerTypes
{

    private static final DeferredRegister<ContainerType<?>> REGISTRAR = DeferredRegister.create(ForgeRegistries.CONTAINERS, ChiselsAndBits.MODID);

    private ModContainerTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModContainerTypes but this is a Utility class.");
    }

    public static final RegistryObject<ContainerType<BagContainer>> BAG_CONTAINER = REGISTRAR.register("bag", () -> new ContainerType<>(BagContainer::new));
    public static final RegistryObject<ContainerType<ChiselStationContainer>> CHISEL_STATION_CONTAINER = REGISTRAR.register("chisel_station", () -> new ContainerType<>(ChiselStationContainer::new));

    public static void onModConstruction() {
        REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
