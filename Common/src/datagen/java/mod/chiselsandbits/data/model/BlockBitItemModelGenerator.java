package mod.chiselsandbits.data.model;

import com.ldtteam.datagenerators.models.item.ItemModelJson;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockBitItemModelGenerator implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new BlockBitItemModelGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private BlockBitItemModelGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(@NotNull final HashCache cache) throws IOException
    {
        actOnBlockWithLoader(cache, new ResourceLocation(Constants.MOD_ID, "bit"), ModItems.ITEM_BLOCK_BIT.get());
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Chisel block item model generator";
    }

    public void actOnBlockWithLoader(final HashCache cache, final ResourceLocation loader, final Item item) throws IOException
    {
        final ItemModelJson json = new ItemModelJson();
        json.setParent("item/generated");
        json.setLoader(loader.toString());

        saveItemJson(cache, json, Objects.requireNonNull(item.getRegistryName()).getPath());
    }

    private void saveItemJson(final HashCache cache, final ItemModelJson json, final String name) throws IOException
    {
        final Path itemModelFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.ITEM_MODEL_DIR);
        final Path itemModelPath = itemModelFolder.resolve(name + ".json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, json.serialize(), itemModelPath);
    }
}
