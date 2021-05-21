package mod.chiselsandbits.client.registrars;

import mod.chiselsandbits.client.besr.BitStorageBESR;
import mod.chiselsandbits.registrars.ModTileEntityTypes;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public final class ModBESR
{

    private ModBESR()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModBESR. This is a utility class");
    }

    public static void onClientInit() {
        ClientRegistry.bindTileEntityRenderer(ModTileEntityTypes.BIT_STORAGE.get(), BitStorageBESR::new);
    }
}
