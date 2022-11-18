package mod.chiselsandbits.forge.data.recipe;

import com.google.gson.JsonObject;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModRecipeSerializers;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SpecialCraftingRecipeGenerator implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(true, new SpecialCraftingRecipeGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private SpecialCraftingRecipeGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(final @NotNull CachedOutput cache) throws IOException
    {
        saveRecipe(cache, ModRecipeSerializers.BAG_DYEING.getId());
    }

    private void saveRecipe(final CachedOutput cache, final ResourceLocation location) throws IOException
    {
        final JsonObject object = new JsonObject();
        object.addProperty("type", location.toString());

        final Path recipeFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.RECIPES_DIR);
        final Path recipePath = recipeFolder.resolve(location.getPath() + ".json");

        DataProvider.saveStable(cache, object, recipePath);
    }

    @Override
    public @NotNull String getName()
    {
        return "Special crafting recipe generator";
    }
}
