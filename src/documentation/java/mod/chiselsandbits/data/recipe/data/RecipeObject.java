package mod.chiselsandbits.data.recipe.data;

import org.spongepowered.asm.mixin.injection.invoke.arg.ArgumentIndexOutOfBoundsException;

import java.util.ArrayList;
import java.util.Collections;
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

    public List<String> getItemFromIndex (int index, final int recipeWidth, final int recipeHeight) {
        if (index < recipeWidth) //First row of the target recipe. We just pass it along.
            return getItemFromIndex(index);

        if (recipeHeight == 1)
            return Collections.emptyList();

        if (recipeWidth < 3)
            index += (3 - recipeWidth);

        index -=3;

        if (index < recipeWidth)
            return getItemFromIndex(3 + index);

        if (recipeHeight == 2)
            return Collections.emptyList();

        if (recipeWidth < 3)
            index += (3 - recipeWidth);

        index -= 3;

        if (index < recipeWidth)
            return getItemFromIndex(6 + index);

        return Collections.emptyList();
    }

    private List<String> getItemFromIndex(final int index) {
        switch(index){
            case 0:
                return getFirstRow().getFirstItem();
            case 1:
                return getFirstRow().getSecondItem();
            case 2:
                return getFirstRow().getThirdItem();
            case 3:
                return getSecondRow().getFirstItem();
            case 4:
                return getSecondRow().getSecondItem();
            case 5:
                return getSecondRow().getThirdItem();
            case 6:
                return getThirdRow().getFirstItem();
            case 7:
                return getThirdRow().getSecondItem();
            case 8:
                return getThirdRow().getThirdItem();
            default:
                throw new ArgumentIndexOutOfBoundsException(index);
        }
    }
}
