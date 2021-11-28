package mod.chiselsandbits.data.recipe.data;

import java.util.ArrayList;
import java.util.List;

public class RecipeRowObject
{
    private List<String> firstItem = new ArrayList<>();
    private List<String> secondItem = new ArrayList<>();
    private List<String> thirdItem = new ArrayList<>();

    public List<String> getFirstItem()
    {
        return firstItem;
    }

    public void setFirstItem(final List<String> firstItem)
    {
        this.firstItem = firstItem;
    }

    public List<String> getSecondItem()
    {
        return secondItem;
    }

    public void setSecondItem(final List<String> secondItem)
    {
        this.secondItem = secondItem;
    }

    public List<String> getThirdItem()
    {
        return thirdItem;
    }

    public void setThirdItem(final List<String> thirdItem)
    {
        this.thirdItem = thirdItem;
    }
}
