package mod.chiselsandbits.client.registrars;

import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.client.colors.BitItemItemColor;
import mod.chiselsandbits.client.colors.ChiseledBlockBlockColor;
import mod.chiselsandbits.client.colors.ChiseledBlockItemItemColor;
import mod.chiselsandbits.item.ChiseledBlockItem;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ModColors
{

    private ModColors()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModColors. This is a utility class");
    }

    public static void onClientInit() {
        Minecraft.getInstance()
          .getBlockColors()
          .register(new ChiseledBlockBlockColor(), ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream().map(RegistryObject::get).toArray(ChiseledBlock[]::new));
        Minecraft.getInstance()
          .getItemColors()
          .register(new ChiseledBlockItemItemColor(), ModItems.MATERIAL_TO_ITEM_CONVERSIONS.values().stream().map(RegistryObject::get).toArray(ChiseledBlockItem[]::new));
        Minecraft.getInstance()
          .getItemColors()
          .register(new BitItemItemColor(), ModItems.ITEM_BLOCK_BIT.get());
    }
}
