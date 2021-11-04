package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistrar;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import mod.chiselsandbits.recipe.BagDyeingRecipe;
import mod.chiselsandbits.recipe.modificationtable.ModificationTableRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;

public final class ModRecipeSerializers
{

    private static final IRegistrar<RecipeSerializer<?>> SERIALIZER_REGISTER = IRegistrar.create(RecipeSerializer.class, Constants.MOD_ID);

    public static IRegistryObject<ModificationTableRecipeSerializer> MODIFICATION_TABLE = SERIALIZER_REGISTER
      .register("modification_table", ModificationTableRecipeSerializer::new);

    public static final IRegistryObject<SimpleRecipeSerializer<BagDyeingRecipe>> BAG_DYEING = SERIALIZER_REGISTER.register("bag_dyeing", () -> new SimpleRecipeSerializer<>(BagDyeingRecipe::new));

    private ModRecipeSerializers()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModRecipeSerializers. This is a utility class");
    }
}
