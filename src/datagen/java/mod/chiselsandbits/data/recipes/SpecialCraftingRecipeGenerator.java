package mod.chiselsandbits.data.recipes;

import com.google.gson.JsonObject;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModRecipeSerializers;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SpecialCraftingRecipeGenerator implements IDataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new SpecialCraftingRecipeGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private SpecialCraftingRecipeGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(final @NotNull DirectoryCache cache) throws IOException
    {
        saveRecipe(cache, ModRecipeSerializers.BAG_DYEING.getId());
    }

    private void saveRecipe(final DirectoryCache cache, final ResourceLocation location) throws IOException
    {
        final JsonObject object = new JsonObject();
        object.addProperty("type", location.toString());

        final Path recipeFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.RECIPES_DIR);
        final Path recipePath = recipeFolder.resolve(location.getPath() + ".json");

        IDataProvider.save(Constants.DataGenerator.GSON, cache, object, recipePath);
    }

    @Override
    public @NotNull String getName()
    {
        return "Special crafting recipe generator";
    }
}
