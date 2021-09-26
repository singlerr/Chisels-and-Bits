package mod.chiselsandbits.client.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.client.colors.BitBagItemColor;
import mod.chiselsandbits.client.colors.BitItemItemColor;
import mod.chiselsandbits.client.colors.ChiseledBlockBlockColor;
import mod.chiselsandbits.client.colors.ChiseledBlockItemItemColor;
import mod.chiselsandbits.item.ChiseledBlockItem;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.RegistryObject;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModColors
{

    private ModColors()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModColors. This is a utility class");
    }

    @SubscribeEvent
    public static void onBlockColorHandler(final ColorHandlerEvent.Block event)
    {
        event.getBlockColors()
          .register(new ChiseledBlockBlockColor(), ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream().map(RegistryObject::get).toArray(ChiseledBlock[]::new));
    }

    @SubscribeEvent
    public static void onItemColorHandler(final ColorHandlerEvent.Item event)
    {
        event.getItemColors()
          .register(new ChiseledBlockItemItemColor(), ModItems.MATERIAL_TO_ITEM_CONVERSIONS.values().stream().map(RegistryObject::get).toArray(ChiseledBlockItem[]::new));
        event.getItemColors()
          .register(new BitItemItemColor(), ModItems.ITEM_BLOCK_BIT.get());
        event.getItemColors()
          .register(new BitBagItemColor(), ModItems.BIT_BAG_DEFAULT.get(), ModItems.ITEM_BIT_BAG_DYED.get());
    }
}
