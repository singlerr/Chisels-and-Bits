package mod.chiselsandbits.data.model;

import com.ldtteam.datagenerators.models.item.ItemModelJson;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.utils.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class QuillItemModelGenerator implements IDataProvider
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
    public void run(final DirectoryCache cache) throws IOException
    {
        final ItemModelJson modelJson = new ItemModelJson();
        final String modelLocation = new ResourceLocation(Constants.MOD_ID, "item/" + Objects.requireNonNull(ModItems.QUILL.get().getRegistryName()).getPath()) + "_spec";

        modelJson.setLoader(Constants.INTERACTABLE_MODEL_LOADER);
        modelJson.setParent(modelLocation);
        final String name = Objects.requireNonNull(ModItems.QUILL.get().getRegistryName()).getPath();
        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(modelJson), generator.getOutputFolder().resolve(DataGeneratorConstants.ITEM_MODEL_DIR).resolve(name + ".json"));
    }

    @Override
    public String getName()
    {
        return "Quill item model generator";
    }
}
