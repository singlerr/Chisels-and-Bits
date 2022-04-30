package mod.chiselsandbits.registrars;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.recipe.modificationtable.ModificationTableRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ModRecipeTypes
{
    private static final Logger                              LOGGER             = LogManager.getLogger();
    public static        RecipeType<ModificationTableRecipe> MODIFICATION_TABLE = register("modification_table");

    /**
     * Registers a new recipe type, prefixing with the mod ID
     *
     * @param name Recipe type name
     * @param <T>  Recipe type
     * @return Registered recipe type
     */
    @SuppressWarnings("SameParameterValue")
    static <T extends Recipe<?>> RecipeType<T> register(String name)
    {
        return RecipeType.register(Constants.MOD_ID + ":" + name);
    }

    public static void onSerializerRegistration() {
        LOGGER.info("Registering recipe types.");
    }
}
