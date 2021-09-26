package mod.chiselsandbits.api.data.tag;

import com.ldtteam.datagenerators.tags.TagJson;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractChiselableTagGenerator implements DataProvider
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
    public void run(final @NotNull HashCache cache) throws IOException
    {
        final TagJson json = new TagJson();
        json.setValues(blocks.stream().map(ForgeRegistryEntry::getRegistryName).filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList()));

        final Path tagFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.BLOCK_TAGS_DIR);
        final Path chiselableTagPath = tagFolder.resolve("chiselable/" + mode.toString().toLowerCase() + ".json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, json.serialize(), chiselableTagPath);
    }

    @Override
    public @NotNull String getName()
    {
        return StringUtils.capitalize(mode.toString().toLowerCase(Locale.ROOT)) + " chiselable tag generator";
    }
}
