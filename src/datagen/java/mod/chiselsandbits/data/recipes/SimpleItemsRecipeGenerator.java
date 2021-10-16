package mod.chiselsandbits.data.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import mod.chiselsandbits.api.data.recipe.AbstractRecipeGenerator;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.registrars.ModTags;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SimpleItemsRecipeGenerator extends AbstractRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(
          new SimpleItemsRecipeGenerator(
            event.getGenerator(),
            ModItems.BIT_BAG_DEFAULT.get(),
            "www;wbw;www",
            ImmutableMap.of(
              "b", new RecipeIngredientKeyJson(new RecipeIngredientJson(ModItems.ITEM_BLOCK_BIT.getId().toString(), false)),
              "w", new RecipeIngredientKeyJson(new RecipeIngredientJson(ItemTags.WOOL.getName().toString(), true))
            )
          )
        );

        event.getGenerator().addProvider(
          new SimpleItemsRecipeGenerator(
            event.getGenerator(),
            ModItems.MAGNIFYING_GLASS.get(),
            "cg ;s  ;   ",
            ImmutableMap.of(
              "c", new RecipeIngredientKeyJson(new RecipeIngredientJson(ModTags.Items.CHISEL.getName().toString(), true)),
              "g", new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.GLASS.getName().toString(), true)),
              "s", new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.RODS_WOODEN.getName().toString(), true))
            )
          )
        );

        event.getGenerator().addProvider(
          new SimpleItemsRecipeGenerator(
            event.getGenerator(),
            ModItems.MEASURING_TAPE.get(),
            "  s;isy;ii ",
            ImmutableMap.of(
              "i", new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.INGOTS_IRON.getName().toString(), true)),
              "s", new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.STRING.getName().toString(), true)),
              "y", new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.DYES_YELLOW.getName().toString(), true))
            )
          )
        );

        event.getGenerator().addProvider(
          new SimpleItemsRecipeGenerator(
            event.getGenerator(),
            ModItems.QUILL.get(),
            ImmutableList.of(
              new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.FEATHERS.getName().toString(), true)),
              new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.DYES_BLACK.getName().toString(), true)),
              new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.DYES_YELLOW.getName().toString(), true))
            )
          )
        );

        event.getGenerator().addProvider(
          new SimpleItemsRecipeGenerator(
            event.getGenerator(),
            ModItems.SEALANT_ITEM.get(),
            ImmutableList.of(
              new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.SLIMEBALLS.getName().toString(), true)),
              new RecipeIngredientKeyJson(new RecipeIngredientJson(Items.HONEY_BOTTLE.getRegistryName().toString(), false))
            )
          )
        );

        event.getGenerator().addProvider(
          new SimpleItemsRecipeGenerator(
            event.getGenerator(),
            ModItems.WRENCH.get(),
            " pb; pp;p  ",
            ImmutableMap.of(
              "p", new RecipeIngredientKeyJson(new RecipeIngredientJson(ItemTags.PLANKS.getName().toString(), true)),
              "b", new RecipeIngredientKeyJson(new RecipeIngredientJson(ModItems.ITEM_BLOCK_BIT.getId().toString(), false))
            )
          )
        );

        event.getGenerator().addProvider(
          new SimpleItemsRecipeGenerator(
            event.getGenerator(),
            ModItems.UNSEAL_ITEM.get(),
            ImmutableList.of(
              new RecipeIngredientKeyJson(new RecipeIngredientJson(Blocks.WET_SPONGE.getRegistryName().toString(), false))
            )
          )
        );
    }

    private final boolean shapeless;

    private final String pattern;
    private final Map<String, RecipeIngredientKeyJson> ingredientKeyJsonMap;

    private final List<RecipeIngredientKeyJson> ingredientKeyJsonList;

    private SimpleItemsRecipeGenerator(final DataGenerator generator, final IItemProvider result, final String pattern, final Map<String, RecipeIngredientKeyJson> ingredientKeyJsonMap)
    {
        super(generator, result);
        this.shapeless = false;
        this.pattern = pattern;
        this.ingredientKeyJsonMap = ingredientKeyJsonMap;
        this.ingredientKeyJsonList = Lists.newArrayList();
    }

    public SimpleItemsRecipeGenerator(
      final DataGenerator generator,
      final IItemProvider itemProvider,
      final List<RecipeIngredientKeyJson> ingredientKeyJsonList)
    {
        super(generator, itemProvider);
        this.shapeless = true;
        this.pattern = "";
        this.ingredientKeyJsonMap = Maps.newHashMap();
        this.ingredientKeyJsonList = ingredientKeyJsonList;
    }

    @Override
    protected void generate() throws IOException
    {
        if (!this.shapeless)
            this.addShapedRecipe(pattern, ingredientKeyJsonMap);
        else
            this.addShapelessRecipe(ingredientKeyJsonList);
    }
}
