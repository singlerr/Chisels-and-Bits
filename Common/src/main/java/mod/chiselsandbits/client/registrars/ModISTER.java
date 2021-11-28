package mod.chiselsandbits.client.registrars;

import mod.chiselsandbits.client.ister.BitStorageISTER;
import mod.chiselsandbits.client.ister.InteractionISTER;
import mod.chiselsandbits.platforms.core.client.IClientManager;
import mod.chiselsandbits.platforms.core.client.rendering.IRenderingManager;
import mod.chiselsandbits.registrars.ModItems;

public class ModISTER
{

    private ModISTER()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModISTER. This is a utility class");
    }

    public static void onClientInit() {
        IRenderingManager.getInstance()
          .registerISTER(
            ModItems.ITEM_BIT_STORAGE.get(), new BitStorageISTER()
          );

        IRenderingManager.getInstance()
          .registerISTER(
            ModItems.UNSEAL_ITEM.get(), new InteractionISTER()
          );

        IRenderingManager.getInstance()
          .registerISTER(
            ModItems.QUILL.get(), new InteractionISTER()
          );

        IRenderingManager.getInstance()
          .registerISTER(
            ModItems.SEALANT_ITEM.get(), new InteractionISTER()
          );
    }
}
