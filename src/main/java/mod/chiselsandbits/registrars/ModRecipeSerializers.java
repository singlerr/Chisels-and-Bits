package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.recipe.BagDyeingRecipe;
import mod.chiselsandbits.recipe.modificationtable.ModificationTableRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModRecipeSerializers
{

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Constants.MOD_ID);

    public static RegistryObject<ModificationTableRecipeSerializer> MODIFICATION_TABLE = SERIALIZER_REGISTER
      .register("modification_table", ModificationTableRecipeSerializer::new);

    public static final RegistryObject<SimpleRecipeSerializer<BagDyeingRecipe>> BAG_DYEING = SERIALIZER_REGISTER.register("bag_dyeing", () -> new SimpleRecipeSerializer<>(BagDyeingRecipe::new));

    private ModRecipeSerializers()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModRecipeSerializers. This is a utility class");
    }

    public static void onModConstruction() {
        SERIALIZER_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
