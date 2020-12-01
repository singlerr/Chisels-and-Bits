package mod.chiselsandbits.data.recipe;

import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import mod.chiselsandbits.items.ItemChisel;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;

import java.io.IOException;

public abstract class AbstractChiselRecipeGenerator extends AbstractRecipeGenerator
{
    private final ITag.INamedTag<?> rodTag;
    private final ITag.INamedTag<?> ingredientTag;

    protected AbstractChiselRecipeGenerator(final DataGenerator generator, final ItemChisel result, final ITag.INamedTag<?> ingredientTag)
    {
        super(generator, result);
        this.ingredientTag = ingredientTag;
        this.rodTag = Tags.Items.RODS_WOODEN;
    }

    protected AbstractChiselRecipeGenerator(
      final DataGenerator generator,
      final ItemChisel result,
      final ITag.INamedTag<?> rodTag,
      final ITag.INamedTag<?> ingredientTag)
    {
        super(generator, result);
        this.rodTag = rodTag;
        this.ingredientTag = ingredientTag;
    }


    @Override
    protected final void generate() throws IOException
    {
        addShapedRecipe(
          "st ",
          "   ",
          "   ",
          "s",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(rodTag.getName().toString(), true)),
          "t",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(ingredientTag.getName().toString(), true))
        );
    }
}
