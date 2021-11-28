package mod.chiselsandbits.data.recipe.data;

import java.util.ArrayList;
import java.util.List;

public class RecipesObject
{
    private List<RecipeObject> recipes = new ArrayList<>();

    public List<RecipeObject> getRecipes()
    {
        return recipes;
    }

    public void setRecipes(final List<RecipeObject> recipes)
    {
        this.recipes = recipes;
    }
}
