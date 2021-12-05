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
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

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

    public ItemLike getItemProvider()
    {
        return itemProvider;
    }

    @Override
    public final @NotNull String getName()
    {
        return Objects.requireNonNull(itemProvider.asItem().getRegistryName()) + " recipe generator";
    }
}