package mod.chiselsandbits.forge.data.model;

import com.ldtteam.datagenerators.models.item.ItemModelJson;
import mod.chiselsandbits.forge.data.DataGeneratorConstants;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SealantItemModelGenerator implements DataProvider
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new SealantItemModelGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    public SealantItemModelGenerator(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void run(final HashCache cache) throws IOException
    {
        final ItemModelJson modelJson = new ItemModelJson();
        final String modelLocation = new ResourceLocation(Constants.MOD_ID, "item/" + Objects.requireNonNull(ModItems.SEALANT_ITEM.get().getRegistryName()).getPath()) + "_spec";

        modelJson.setLoader(Constants.INTERACTABLE_MODEL_LOADER);
        modelJson.setParent(modelLocation);
        final String name = Objects.requireNonNull(ModItems.SEALANT_ITEM.get().getRegistryName()).getPath();
        DataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(modelJson), generator.getOutputFolder().resolve(DataGeneratorConstants.ITEM_MODEL_DIR).resolve(name + ".json"));
    }

    @Override
    public String getName()
    {
        return "Sealant item model generator";
    }
}
