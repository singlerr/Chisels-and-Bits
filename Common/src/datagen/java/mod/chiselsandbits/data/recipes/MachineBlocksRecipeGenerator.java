package mod.chiselsandbits.data.recipes;

import com.google.common.collect.ImmutableMap;
import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import mod.chiselsandbits.api.data.recipe.AbstractRecipeGenerator;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.registrars.ModTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MachineBlocksRecipeGenerator extends AbstractRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(
          new MachineBlocksRecipeGenerator(
            event.getGenerator(),
            ModBlocks.CHISELED_PRINTER.get(),
            " c ;t t;sss",
            ImmutableMap.of(
              "c", new RecipeIngredientKeyJson(new RecipeIngredientJson(ModTags.Items.CHISEL.getName().toString(), true)),
              "t", new RecipeIngredientKeyJson(new RecipeIngredientJson(Objects.requireNonNull(Blocks.SMOOTH_STONE_SLAB.getRegistryName()).toString(), false)),
              "s", new RecipeIngredientKeyJson(new RecipeIngredientJson(ModTags.Items.CHISEL.getName().toString(), true))
            )
          )
        );

        event.getGenerator().addProvider(
          new MachineBlocksRecipeGenerator(
            event.getGenerator(),
            ModBlocks.MODIFICATION_TABLE.get(),
            "scs;nbn;ppp",
            ImmutableMap.of(
              "s", new RecipeIngredientKeyJson(new RecipeIngredientJson(ItemTags.WOODEN_SLABS.getName().toString(), true)),
              "n", new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.NUGGETS_IRON.getName().toString(), true)),
              "b", new RecipeIngredientKeyJson(new RecipeIngredientJson(ItemTags.LOGS.getName().toString(), true)),
              "p", new RecipeIngredientKeyJson(new RecipeIngredientJson(ItemTags.PLANKS.getName().toString(), true)),
              "c", new RecipeIngredientKeyJson(new RecipeIngredientJson(ModTags.Items.CHISEL.getName().toString(), true))
            )
          )
        );

        event.getGenerator().addProvider(
          new MachineBlocksRecipeGenerator(
            event.getGenerator(),
            ModBlocks.BIT_STORAGE.get(),
            "igi;glg;ici",
            ImmutableMap.of(
              "g", new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.GLASS.getName().toString(), true)),
              "l", new RecipeIngredientKeyJson(new RecipeIngredientJson(ItemTags.LOGS.getName().toString(), true)),
              "i", new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.INGOTS_IRON.getName().toString(), true)),
              "c", new RecipeIngredientKeyJson(new RecipeIngredientJson(ModTags.Items.CHISEL.getName().toString(), true))
            )
          )
        );
    }

    private final String pattern;
    private final Map<String, RecipeIngredientKeyJson> ingredientKeyJsonMap;

    private MachineBlocksRecipeGenerator(final DataGenerator generator, final ItemLike result, final String pattern, final Map<String, RecipeIngredientKeyJson> ingredientKeyJsonMap)
    {
        super(generator, result);
        this.pattern = pattern;
        this.ingredientKeyJsonMap = ingredientKeyJsonMap;
    }

    @Override
    protected void generate() throws IOException
    {
        this.addShapedRecipe(pattern, ingredientKeyJsonMap);
    }
}
