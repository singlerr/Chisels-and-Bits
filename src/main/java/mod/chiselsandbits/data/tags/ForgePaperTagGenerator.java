package mod.chiselsandbits.data.tags;

import com.google.common.collect.Lists;
import com.ldtteam.datagenerators.tags.TagJson;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.data.model.ChiseledBlockItemModelGenerator;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgePaperTagGenerator implements IDataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ForgePaperTagGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ForgePaperTagGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void act(final DirectoryCache cache) throws IOException
    {
        final TagJson tagJson = new TagJson();
        tagJson.setReplace(false);
        tagJson.setValues(Lists.newArrayList(Objects.requireNonNull(Items.PAPER.getRegistryName()).toString()));

        final Path forgeTagFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.FORGE_ITEM_TAGS_DIR);
        final Path forgePaperTagPath = forgeTagFolder.resolve("paper.json");

        IDataProvider.save(Constants.DataGenerator.GSON, cache, tagJson.serialize(), forgePaperTagPath);
    }

    @Override
    public String getName()
    {
        return "Forge paper tag generator";
    }
}
