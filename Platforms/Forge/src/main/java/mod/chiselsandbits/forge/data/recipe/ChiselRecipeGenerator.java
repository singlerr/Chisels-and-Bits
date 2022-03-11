package mod.chiselsandbits.forge.data.recipe;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiselRecipeGenerator extends AbstractChiselRecipeGenerator
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ChiselRecipeGenerator(event.getGenerator(), ModItems.ITEM_CHISEL_STONE.get(), Tags.Items.STONE));
        event.getGenerator().addProvider(new ChiselRecipeGenerator(event.getGenerator(), ModItems.ITEM_CHISEL_IRON.get(), Tags.Items.INGOTS_IRON));
        event.getGenerator().addProvider(new ChiselRecipeGenerator(event.getGenerator(), ModItems.ITEM_CHISEL_GOLD.get(), Tags.Items.INGOTS_GOLD));
        event.getGenerator().addProvider(new ChiselRecipeGenerator(event.getGenerator(), ModItems.ITEM_CHISEL_DIAMOND.get(), Tags.Items.GEMS_DIAMOND));
        event.getGenerator().addProvider(new ChiselRecipeGenerator(event.getGenerator(), ModItems.ITEM_CHISEL_NETHERITE.get(), Tags.Items.RODS_BLAZE, Tags.Items.INGOTS_NETHERITE));
    }

    private ChiselRecipeGenerator(
      final DataGenerator generator,
      final Item result,
      final TagKey<Item> ingredientTag)
    {
        super(generator, result, ingredientTag);
    }

    private ChiselRecipeGenerator(final DataGenerator generator, final Item result, final TagKey<Item> rodTag, final TagKey<Item> ingredientTag)
    {
        super(generator, result, rodTag, ingredientTag);
    }
}
