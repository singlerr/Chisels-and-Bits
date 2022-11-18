package mod.chiselsandbits.forge.data.recipe;

import com.google.gson.JsonObject;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModModificationOperation;
import mod.chiselsandbits.registrars.ModRecipeSerializers;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModificationOperationRecipeProvider extends RecipeProvider
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(true, new ModificationOperationRecipeProvider(event.getGenerator()));
    }

    private ModificationOperationRecipeProvider(final DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @Override
    protected void buildCraftingRecipes(final @NotNull Consumer<FinishedRecipe> consumer)
    {
        ModModificationOperation.REGISTRY_SUPPLIER.get().forEach(
          operation -> consumer.accept(new FinishedRecipe() {
              @Override
              public void serializeRecipeData(final @NotNull JsonObject json)
              {

              }

              @Override
              public @NotNull ResourceLocation getId()
              {
                  return Objects.requireNonNull(operation.getRegistryName());
              }

              @Override
              public @NotNull RecipeSerializer<?> getType()
              {
                  return ModRecipeSerializers.MODIFICATION_TABLE.get();
              }

              @Nullable
              @Override
              public JsonObject serializeAdvancement()
              {
                  return null;
              }

              @Nullable
              @Override
              public ResourceLocation getAdvancementId()
              {
                  return null;
              }
          })
        );
    }

    @Override
    public @NotNull String getName()
    {
        return "Modification operation recipes";
    }
}
