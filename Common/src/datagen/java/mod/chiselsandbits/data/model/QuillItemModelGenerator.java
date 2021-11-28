package mod.chiselsandbits.data.model;

import com.ldtteam.datagenerators.models.item.ItemModelJson;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.utils.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class QuillItemModelGenerator implements DataProvider
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new QuillItemModelGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    public QuillItemModelGenerator(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void run(final HashCache cache) throws IOException
    {
        final ItemModelJson modelJson = new ItemModelJson();
        final String modelLocation = new ResourceLocation(Constants.MOD_ID, "item/" + Objects.requireNonNull(ModItems.QUILL.get().getRegistryName()).getPath()) + "_spec";

        modelJson.setLoader(Constants.INTERACTABLE_MODEL_LOADER);
        modelJson.setParent(modelLocation);
        final String name = Objects.requireNonNull(ModItems.QUILL.get().getRegistryName()).getPath();
        DataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(modelJson), generator.getOutputFolder().resolve(DataGeneratorConstants.ITEM_MODEL_DIR).resolve(name + ".json"));
    }

    @Override
    public String getName()
    {
        return "Quill item model generator";
    }
}
