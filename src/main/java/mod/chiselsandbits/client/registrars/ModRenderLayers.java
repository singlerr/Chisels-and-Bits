package mod.chiselsandbits.client.registrars;

import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.fmllegacy.RegistryObject;

public final class ModRenderLayers
{

    private ModRenderLayers()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModRenderLayers. This is a utility class");
    }

    public static void onClientInit() {
        ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values()
          .stream().map(RegistryObject::get)
          .forEach(b -> ItemBlockRenderTypes.setRenderLayer(b, input -> RenderType.chunkBufferLayers().contains(input)));

        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BIT_STORAGE.get(), RenderType.cutoutMipped());

        ItemBlockRenderTypes.setRenderLayer(ModBlocks.CHISELED_PRINTER.get(), RenderType.cutoutMipped());
    }
}
