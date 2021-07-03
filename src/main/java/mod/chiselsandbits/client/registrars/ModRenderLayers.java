package mod.chiselsandbits.client.registrars;

import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.RegistryObject;

public final class ModRenderLayers
{

    private ModRenderLayers()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModRenderLayers. This is a utility class");
    }

    public static void onClientInit() {
        ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values()
          .stream().map(RegistryObject::get)
          .forEach(b -> RenderTypeLookup.setRenderLayer(b, input -> RenderType.getBlockRenderTypes().contains(input)));

        RenderTypeLookup.setRenderLayer(ModBlocks.BIT_STORAGE.get(), RenderType.getCutoutMipped());

        RenderTypeLookup.setRenderLayer(ModBlocks.CHISELED_PRINTER.get(), RenderType.getCutoutMipped());
    }
}
