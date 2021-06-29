package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.recipe.modificationtable.ModificationTableRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;

public final class ModRecipeTypes
{
    public static IRecipeType<ModificationTableRecipe> MODIFICATION_TABLE = register("modification_table");

    /**
     * Registers a new recipe type, prefixing with the mod ID
     * @param name  Recipe type name
     * @param <T>   Recipe type
     * @return  Registered recipe type
     */
    @SuppressWarnings("SameParameterValue")
    static <T extends IRecipe<?>> IRecipeType<T> register(String name) {
        return IRecipeType.register(Constants.MOD_ID + ":" + name);
    }
}
