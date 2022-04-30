package mod.chiselsandbits.client.registrars;

import mod.chiselsandbits.client.besr.BitStorageBESR;
import mod.chiselsandbits.registrars.ModBlockEntityTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class ModBESR
{

    private ModBESR()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModBESR. This is a utility class");
    }

    public static void onClientInit() {
        BlockEntityRenderers.register(ModBlockEntityTypes.BIT_STORAGE.get(), context -> new BitStorageBESR());
    }
}
