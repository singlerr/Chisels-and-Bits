package mod.chiselsandbits.data.recipes;

import com.google.gson.JsonObject;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.data.blockstate.ModificationTableBlockStateGenerator;
import mod.chiselsandbits.registrars.ModModificationOperation;
import mod.chiselsandbits.registrars.ModRecipeSerializers;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModificationTableRecipeProvider extends RecipeProvider
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ModificationTableRecipeProvider(event.getGenerator()));
    }

    private ModificationTableRecipeProvider(final DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(final @NotNull Consumer<IFinishedRecipe> consumer)
    {
        ModModificationOperation.REGISTRY_SUPPLIER.get().forEach(
          operation -> {
              consumer.accept(new IFinishedRecipe() {
                  @Override
                  public void serialize(final @NotNull JsonObject json)
                  {

                  }

                  @Override
                  public @NotNull ResourceLocation getID()
                  {
                      return Objects.requireNonNull(operation.getRegistryName());
                  }

                  @Override
                  public @NotNull IRecipeSerializer<?> getSerializer()
                  {
                      return ModRecipeSerializers.MODIFICATION_TABLE.get();
                  }

                  @Nullable
                  @Override
                  public JsonObject getAdvancementJson()
                  {
                      return null;
                  }

                  @Nullable
                  @Override
                  public ResourceLocation getAdvancementID()
                  {
                      return null;
                  }
              });
          }
        );
    }

    @Override
    public @NotNull String getName()
    {
        return "Modification table recipes";
    }
}
