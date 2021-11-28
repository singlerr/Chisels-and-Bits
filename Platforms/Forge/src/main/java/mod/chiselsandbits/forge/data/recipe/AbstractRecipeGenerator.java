package mod.chiselsandbits.forge.data.recipe;

import com.google.common.collect.ImmutableMap;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import com.ldtteam.datagenerators.recipes.RecipeResultJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedPatternJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedRecipeJson;
import com.ldtteam.datagenerators.recipes.shapeless.ShapelessRecipeJson;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public abstract class AbstractRecipeGenerator extends RecipeProvider
{
    private final DataGenerator    generator;
    private final ItemLike itemProvider;

    private HashCache cache = null;

    protected AbstractRecipeGenerator(final DataGenerator generator, final ItemLike itemProvider) {
        super(generator);
        this.generator = generator;
        this.itemProvider = itemProvider;
    }



    protected abstract void generate() throws IOException;

    protected final void addShapedRecipe(
      final String upperPart,
      final String middlePart,
      final String lowerPart,
      final String keyOne,
      final RecipeIngredientKeyJson ingOne
    ) throws IOException
    {
        final ShapedRecipeJson json = createShaped(upperPart, middlePart, lowerPart);
        json.setKey(ImmutableMap.of(keyOne, ingOne));

        save(json);
    }

    protected final void addShapedRecipe(
      final String upperPart,
      final String middlePart,
      final String lowerPart,
      final String keyOne,
      final RecipeIngredientKeyJson ingOne,
      final String keyTwo,
      final RecipeIngredientKeyJson ingTwo
    ) throws IOException
    {
        final ShapedRecipeJson json = createShaped(upperPart, middlePart, lowerPart);
        json.setKey(ImmutableMap.of(keyOne, ingOne, keyTwo, ingTwo));

        save(json);
    }

    protected final void addShapedRecipe(
      final String upperPart,
      final String middlePart,
      final String lowerPart,
      final String keyOne,
      final RecipeIngredientKeyJson ingOne,
      final String keyTwo,
      final RecipeIngredientKeyJson ingTwo,
      final String keyThree,
      final RecipeIngredientKeyJson ingThree
    ) throws IOException
    {
        final ShapedRecipeJson json = createShaped(upperPart, middlePart, lowerPart);
        json.setKey(ImmutableMap.of(keyOne, ingOne, keyTwo, ingTwo, keyThree, ingThree));

        save(json);
    }

    protected final void addShapedRecipe(
      final String upperPart,
      final String middlePart,
      final String lowerPart,
      final String keyOne,
      final RecipeIngredientKeyJson ingOne,
      final String keyTwo,
      final RecipeIngredientKeyJson ingTwo,
      final String keyThree,
      final RecipeIngredientKeyJson ingThree,
      final String keyFour,
      final RecipeIngredientKeyJson ingFour
    ) throws IOException
    {
        final ShapedRecipeJson json = createShaped(upperPart, middlePart, lowerPart);
        json.setKey(ImmutableMap.of(keyOne, ingOne, keyTwo, ingTwo, keyThree, ingThree, keyFour, ingFour));

        save(json);
    }

    protected final void addShapedRecipe(
      final String pattern,
      final Map<String, RecipeIngredientKeyJson> ingredientKeyJsonMap
    ) throws IOException
    {
        final String[] patternSections = pattern.split(";");
        final ShapedRecipeJson json = createShaped(
          patternSections[0],
          patternSections.length > 1 ? patternSections[1] : "",
          patternSections.length > 2 ? patternSections[2] : ""
        );
        json.setKey(ingredientKeyJsonMap);

        save(json);
    }

    protected final void addShapelessRecipe(
      final List<RecipeIngredientKeyJson> ingredientKeyJsonMap
    ) throws IOException
    {
        final ShapelessRecipeJson json = createShapeless();
        json.setIngredients(ingredientKeyJsonMap);

        save(json);
    }

    private ShapedRecipeJson createShaped(
      final String upperPart,
      final String middlePart,
      final String lowerPart
    ) {
        final ShapedRecipeJson json = new ShapedRecipeJson();
        json.setGroup(Constants.MOD_ID);
        json.setRecipeType(ShapedRecipeJson.getDefaultType());
        json.setPattern(new ShapedPatternJson(upperPart, middlePart, lowerPart));
        return json;
    }

    private ShapelessRecipeJson createShapeless(
    ) {
        final ShapelessRecipeJson json = new ShapelessRecipeJson();
        json.setGroup(Constants.MOD_ID);
        json.setRecipeType(ShapelessRecipeJson.getDefaultType());
        return json;
    }

    private void save(final ShapedRecipeJson shapedRecipeJson) throws IOException
    {
        shapedRecipeJson.setResult(new RecipeResultJson(1, Objects.requireNonNull(this.itemProvider.asItem().getRegistryName()).toString()));

        final Path recipeFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.RECIPES_DIR);
        final Path recipePath = recipeFolder.resolve(Objects.requireNonNull(this.itemProvider.asItem().getRegistryName()).getPath() + ".json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, shapedRecipeJson.serialize(), recipePath);
    }

    private void save(final ShapelessRecipeJson shapelessRecipeJson) throws IOException
    {
        shapelessRecipeJson.setResult(new RecipeResultJson(1, Objects.requireNonNull(this.itemProvider.asItem().getRegistryName()).toString()));

        final Path recipeFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.RECIPES_DIR);
        final Path recipePath = recipeFolder.resolve(Objects.requireNonNull(this.itemProvider.asItem().getRegistryName()).getPath() + ".json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, shapelessRecipeJson.serialize(), recipePath);
    }

    @Override
    public final @NotNull String getName()
    {
        return Objects.requireNonNull(itemProvider.asItem().getRegistryName()) + " recipe generator";
    }
}