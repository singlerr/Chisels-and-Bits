package mod.chiselsandbits.integration.chiselsandbits.create;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CreateClient
{
    public static void clientInit() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get()
          .getModEventBus();

        modEventBus.addListener(CreateMaterials::flwInit);
    }
}
