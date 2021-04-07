package mod.chiselsandbits.data.model;

import com.ldtteam.datagenerators.models.item.ItemModelJson;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockBitItemModelGenerator implements IDataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new BlockBitItemModelGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private BlockBitItemModelGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        actOnItemWithEmptyGenerated(cache, ModItems.ITEM_BLOCK_BIT.get());
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Chisel block item model generator";
    }

    public void actOnItemWithEmptyGenerated(final DirectoryCache cache, final Item item) throws IOException
    {
        final ItemModelJson json = new ItemModelJson();
        json.setParent("item/generated");

        saveItemJson(cache, json, Objects.requireNonNull(item.getRegistryName()).getPath());
    }

    private void saveItemJson(final DirectoryCache cache, final ItemModelJson json, final String name) throws IOException
    {
        final Path itemModelFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.ITEM_MODEL_DIR);
        final Path itemModelPath = itemModelFolder.resolve(name + ".json");

        IDataProvider.save(Constants.DataGenerator.GSON, cache, json.serialize(), itemModelPath);
    }
}
