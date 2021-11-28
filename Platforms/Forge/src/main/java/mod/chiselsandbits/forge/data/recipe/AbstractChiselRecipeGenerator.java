package mod.chiselsandbits.forge.data.recipe;

import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import mod.chiselsandbits.api.item.chisel.IChiselItem;
import mod.chiselsandbits.api.util.ParamValidator;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

import java.io.IOException;

public abstract class AbstractChiselRecipeGenerator extends AbstractRecipeGenerator
{
    private final Tag.Named<?> rodTag;
    private final Tag.Named<?> ingredientTag;

    protected AbstractChiselRecipeGenerator(final DataGenerator generator, final Item result, final Tag.Named<?> ingredientTag)
    {
        super(generator, ParamValidator.isInstanceOf(result, IChiselItem.class));
        this.ingredientTag = ingredientTag;
        this.rodTag = Tags.Items.RODS_WOODEN;
    }

    protected AbstractChiselRecipeGenerator(
      final DataGenerator generator,
      final Item result,
      final Tag.Named<?> rodTag,
      final Tag.Named<?> ingredientTag)
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