package mod.chiselsandbits.data.tags;

import com.ldtteam.datagenerators.tags.TagJson;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractChiselableTagGenerator implements IDataProvider
{

    public enum Mode
    {
        FORCED,
        BLOCKED
    }

    private final DataGenerator generator;
    private final Mode        mode;
    private final List<Block> blocks;

    protected AbstractChiselableTagGenerator(final DataGenerator generator, final Mode mode, final List<Block> blocks) {
        this.generator = generator;
        this.mode = mode;
        this.blocks = blocks;
    }


    @Override
    public void act(final DirectoryCache cache) throws IOException
    {
        final TagJson json = new TagJson();
        json.setValues(blocks.stream().map(ForgeRegistryEntry::getRegistryName).filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList()));

        final Path tagFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.BLOCK_TAGS_DIR);
        final Path chiselableTagPath = tagFolder.resolve("chiselable/" + mode.toString().toLowerCase() + ".json");

        IDataProvider.save(Constants.DataGenerator.GSON, cache, json.serialize(), chiselableTagPath);
    }

    @Override
    public String getName()
    {
        return StringUtils.capitalize(mode.toString()) + " chiselable tag generator";
    }
}
