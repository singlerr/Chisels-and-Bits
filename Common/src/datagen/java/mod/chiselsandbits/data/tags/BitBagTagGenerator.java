package mod.chiselsandbits.data.tags;

import com.google.common.collect.Lists;
import com.ldtteam.datagenerators.tags.TagJson;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BitBagTagGenerator implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new BitBagTagGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private BitBagTagGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(final @NotNull HashCache cache) throws IOException
    {
        final TagJson json = new TagJson();
        json.setValues(
          Lists.newArrayList(
            ModItems.BIT_BAG_DEFAULT.getId().toString(),
            ModItems.ITEM_BIT_BAG_DYED.getId().toString()
          )
        );

        final Path tagFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.ITEM_TAGS_DIR);
        final Path chiselableTagPath = tagFolder.resolve("bit_bag.json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, json.serialize(), chiselableTagPath);
    }

    @Override
    public @NotNull String getName()
    {
        return "BitBag tag generator";
    }
}
