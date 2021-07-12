package mod.chiselsandbits.data.recipe.data;

import java.util.ArrayList;
import java.util.List;

public class RecipeObject
{
    private RecipeRowObject firstRow    = new RecipeRowObject();
    private RecipeRowObject secondRow   = new RecipeRowObject();
    private RecipeRowObject thirdRow    = new RecipeRowObject();
    private List<String>    products     = new ArrayList<>();
    private boolean         isShapeless = false;

    public RecipeRowObject getFirstRow()
    {
        return firstRow;
    }

    public void setFirstRow(final RecipeRowObject firstRow)
    {
        this.firstRow = firstRow;
    }

    public RecipeRowObject getSecondRow()
    {
        return secondRow;
    }

    public void setSecondRow(final RecipeRowObject secondRow)
    {
        this.secondRow = secondRow;
    }

    public RecipeRowObject getThirdRow()
    {
        return thirdRow;
    }

    public void setThirdRow(final RecipeRowObject thirdRow)
    {
        this.thirdRow = thirdRow;
    }

    public List<String> getProducts()
    {
        return products;
    }

    public void setProducts(final List<String> products)
    {
        this.products = products;
    }

    public boolean isShapeless()
    {
        return isShapeless;
    }

    public void setShapeless(final boolean shapeless)
    {
        isShapeless = shapeless;
    }
}
