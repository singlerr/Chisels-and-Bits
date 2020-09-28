package mod.chiselsandbits.data.recipe;

import com.google.common.collect.Lists;
import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import com.ldtteam.datagenerators.recipes.RecipeResultJson;
import com.ldtteam.datagenerators.recipes.shapeless.ShapelessRecipeJson;
import mod.chiselsandbits.interfaces.IPatternItem;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public abstract class AbstractPrintRecipeGenerator<ITEM extends Item & IPatternItem> implements IDataProvider
{
    private final DataGenerator generator;
    private final ITEM item;
    private final ITEM printedItem;
    private final ITag.INamedTag<Item> primaryIngredient;

    protected AbstractPrintRecipeGenerator(final DataGenerator generator, final ITEM item, final ITEM printedItem, final ITag.INamedTag<Item> primaryIngredient) {
        this.generator = generator;
        this.item = item;
        this.printedItem = printedItem;
        this.primaryIngredient = primaryIngredient;
    }

    @Override
    public final void act(final DirectoryCache cache) throws IOException
    {
        generateInitialRecipe(cache);
        generateCleanResettingRecipe(cache);
        generatePrintedResettingRecipe(cache);
    }

    private void generateInitialRecipe(final DirectoryCache cache) throws IOException
    {
        final ShapelessRecipeJson shapelessRecipeJson = new ShapelessRecipeJson();
        shapelessRecipeJson.setGroup(Constants.MOD_ID);
        shapelessRecipeJson.setRecipeType(ShapelessRecipeJson.getDefaultType());
        shapelessRecipeJson.setIngredients(
          Lists.newArrayList(
            new RecipeIngredientKeyJson(
              new RecipeIngredientJson(
                "forge:paper",
                true
              )
            ),
            new RecipeIngredientKeyJson(
              new RecipeIngredientJson(
                Objects.requireNonNull(Items.WATER_BUCKET.getRegistryName()).toString(),
                false
              )
            ),
            new RecipeIngredientKeyJson(
              new RecipeIngredientJson(
                primaryIngredient.getName().toString(),
                true
              )
            )
          )
        );
        shapelessRecipeJson.setResult(
          new RecipeResultJson(
            1,
            Objects.requireNonNull(item.getRegistryName()).toString()
          )
        );

        final Path recipeFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.RECIPES_DIR);
        final Path initialRecipeFolder = recipeFolder.resolve(item.getRegistryName().getPath() + "_initial.json");

        IDataProvider.save(Constants.DataGenerator.GSON, cache, shapelessRecipeJson.serialize(), initialRecipeFolder);
    }

    private void generateCleanResettingRecipe(final DirectoryCache cache) throws IOException
    {
        generateResettingRecipe(cache, item, "resetting");
    }

    private void generatePrintedResettingRecipe(final DirectoryCache cache) throws IOException
    {
        generateResettingRecipe(cache, printedItem, "cleaning");
    }

    private void generateResettingRecipe(final DirectoryCache cache, final ITEM printedItem, final String suffix) throws IOException
    {
        final ShapelessRecipeJson shapelessRecipeJson = new ShapelessRecipeJson();
        shapelessRecipeJson.setGroup(Constants.MOD_ID);
        shapelessRecipeJson.setRecipeType(ShapelessRecipeJson.getDefaultType());
        shapelessRecipeJson.setIngredients(
          Lists.newArrayList(
            new RecipeIngredientKeyJson(
              new RecipeIngredientJson(
                Objects.requireNonNull(printedItem.getRegistryName()).toString(),
                false
              )
            )
          )
        );
        shapelessRecipeJson.setResult(
          new RecipeResultJson(
            1,
            Objects.requireNonNull(item.getRegistryName()).toString()
          )
        );

        final Path recipeFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.RECIPES_DIR);
        final Path initialRecipeFolder = recipeFolder.resolve(item.getRegistryName().getPath() + "_" + suffix + ".json");

        IDataProvider.save(Constants.DataGenerator.GSON, cache, shapelessRecipeJson.serialize(), initialRecipeFolder);
    }

    @Override
    public String getName()
    {
        return Objects.requireNonNull(item.getRegistryName()).toString() + " recipe generator";
    }
}
