package mod.chiselsandbits.api.data.recipe;

import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import mod.chiselsandbits.api.item.chisel.IChiselItem;
import mod.chiselsandbits.api.util.ParamValidator;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;

import java.io.IOException;

public abstract class AbstractChiselRecipeGenerator extends AbstractRecipeGenerator
{
    private final ITag.INamedTag<?> rodTag;
    private final ITag.INamedTag<?> ingredientTag;

    protected AbstractChiselRecipeGenerator(final DataGenerator generator, final Item result, final ITag.INamedTag<?> ingredientTag)
    {
        super(generator, ParamValidator.isInstanceOf(result, IChiselItem.class));
        this.ingredientTag = ingredientTag;
        this.rodTag = Tags.Items.RODS_WOODEN;
    }

    protected AbstractChiselRecipeGenerator(
      final DataGenerator generator,
      final Item result,
      final ITag.INamedTag<?> rodTag,
      final ITag.INamedTag<?> ingredientTag)
    {
        super(generator, ParamValidator.isInstanceOf(result, IChiselItem.class));
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