package mod.chiselsandbits.client.registrars;

import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.client.colors.BitBagItemColor;
import mod.chiselsandbits.client.colors.BitItemItemColor;
import mod.chiselsandbits.client.colors.ChiseledBlockBlockColor;
import mod.chiselsandbits.client.colors.ChiseledBlockItemItemColor;
import mod.chiselsandbits.item.ChiseledBlockItem;
import mod.chiselsandbits.platforms.core.client.rendering.IColorManager;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.registrars.ModItems;

public final class ModColors
{

    private ModColors()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModColors. This is a utility class");
    }

    public static void onBlockColorHandler()
    {
        IColorManager.getInstance().setupBlockColors(
          configuration -> configuration.register(
            new ChiseledBlockBlockColor(),
            ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream().map(IRegistryObject::get).toArray(ChiseledBlock[]::new)
          )
        );
    }

    public static void onItemColorHandler()
    {
        IColorManager.getInstance().setupItemColors(
          configuration -> {
              configuration
                .register(new ChiseledBlockItemItemColor(), ModItems.MATERIAL_TO_ITEM_CONVERSIONS.values().stream().map(IRegistryObject::get).toArray(ChiseledBlockItem[]::new));
              configuration
                .register(new BitItemItemColor(), ModItems.ITEM_BLOCK_BIT.get());
              configuration
                .register(new BitBagItemColor(), ModItems.BIT_BAG_DEFAULT.get(), ModItems.ITEM_BIT_BAG_DYED.get());
          }
        );
    }
}
