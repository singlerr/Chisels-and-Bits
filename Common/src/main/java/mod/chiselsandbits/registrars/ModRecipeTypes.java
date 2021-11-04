package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.recipe.modificationtable.ModificationTableRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public final class ModRecipeTypes
{
    public static RecipeType<ModificationTableRecipe> MODIFICATION_TABLE = register("modification_table");

    /**
     * Registers a new recipe type, prefixing with the mod ID
     * @param name  Recipe type name
     * @param <T>   Recipe type
     * @return  Registered recipe type
     */
    @SuppressWarnings("SameParameterValue")
    static <T extends Recipe<?>> RecipeType<T> register(String name) {
        return RecipeType.register(Constants.MOD_ID + ":" + name);
    }
}
