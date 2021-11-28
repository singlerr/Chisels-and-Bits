package mod.chiselsandbits.data.tags;

import com.google.common.collect.Lists;
import com.ldtteam.datagenerators.tags.TagJson;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiseledBlockTagGenerator implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ChiseledBlockTagGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ChiseledBlockTagGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(@NotNull final HashCache cache) throws IOException
    {
        final TagJson json = new TagJson();
        json.setValues(
          ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream()
            .map(RegistryObject::get)
            .map(Block::getRegistryName)
            .map(Object::toString)
            .collect(Collectors.toList())
        );

        final Path tagFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.BLOCK_TAGS_DIR);
        final Path chiselableTagPath = tagFolder.resolve("chiseled/block.json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, json.serialize(), chiselableTagPath);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Chiseled block tag generator";
    }
}
