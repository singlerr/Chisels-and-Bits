package mod.chiselsandbits.data.recipe.data;

import java.util.ArrayList;
import java.util.List;

public class ItemWikiDataObject
{
    private List<String> recipes = new ArrayList<>();
    private List<String> usages = new ArrayList<>();

    public List<String> getRecipes()
    {
        return recipes;
    }

    public void setRecipes(final List<String> recipes)
    {
        this.recipes = recipes;
    }

    public List<String> getUsages()
    {
        return usages;
    }

    public void setUsages(final List<String> usages)
    {
        this.usages = usages;
    }
}
