package mod.chiselsandbits.client.registrars;

import mod.chiselsandbits.platforms.core.client.rendering.type.IRenderTypeManager;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.client.renderer.RenderType;

public final class ModRenderLayers
{

    private ModRenderLayers()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModRenderLayers. This is a utility class");
    }

    public static void onClientInit() {
        ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values()
          .stream().map(IRegistryObject::get)
          .forEach(b -> IRenderTypeManager.getInstance().setPossibleRenderTypesFor(b, RenderType.translucent(), input -> RenderType.chunkBufferLayers().contains(input)));

        IRenderTypeManager.getInstance().setPossibleRenderTypesFor(ModBlocks.BIT_STORAGE.get(), RenderType.cutoutMipped());

        IRenderTypeManager.getInstance().setPossibleRenderTypesFor(ModBlocks.CHISELED_PRINTER.get(), RenderType.cutoutMipped());
    }
}
