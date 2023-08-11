package mod.chiselsandbits.client.registrars;

import com.communi.suggestu.scena.core.client.rendering.IColorManager;
import com.communi.suggestu.scena.core.registries.deferred.IRegistryObject;
import mod.chiselsandbits.api.block.IBlockConstructionManager;
import mod.chiselsandbits.client.colors.BitBagItemColor;
import mod.chiselsandbits.client.colors.BitItemItemColor;
import mod.chiselsandbits.client.colors.ChiseledBlockBlockColor;
import mod.chiselsandbits.client.colors.ChiseledBlockItemItemColor;
import mod.chiselsandbits.client.icon.IconManager;
import mod.chiselsandbits.client.reloading.ClientResourceReloadingManager;
import mod.chiselsandbits.item.ChiseledBlockItem;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;

public final class BlockColors
{

    private BlockColors()
    {
        throw new IllegalStateException("Can not instantiate an instance of: BlockColors. This is a utility class");
    }

    public static void onClientConstruction()
    {
        IColorManager.getInstance().setupBlockColors(
          configuration -> {
              configuration.register(
                      new ChiseledBlockBlockColor(),
                      ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream().map(IRegistryObject::get).toArray(Block[]::new)
              );
              configuration.register(
                      new ChiseledBlockBlockColor(),
                      ModBlocks.CHISELED_BLOCK.get()
              );
          }
        );
    }
}
