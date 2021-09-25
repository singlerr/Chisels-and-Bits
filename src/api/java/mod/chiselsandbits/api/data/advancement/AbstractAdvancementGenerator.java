package mod.chiselsandbits.api.data.advancement;

import com.google.common.collect.Sets;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractAdvancementGenerator implements IDataProvider
{

    private static final Logger LOGGER = LogManager.getLogger();

    private final DataGenerator generator;
    private final Consumer<Consumer<Advancement>> advancementProvider;

    public AbstractAdvancementGenerator(
      final DataGenerator generator,
      final Consumer<Consumer<Advancement>> advancementProvider) {this.generator = generator;
        this.advancementProvider = advancementProvider;
    }

    @Override
    public void run(final @NotNull DirectoryCache cache) throws IOException
    {
        Path outputFolder = this.generator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> consumer = (advancement) -> {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            } else {
                Path path1 = getPath(outputFolder, advancement);

                try {
                    IDataProvider.save(Constants.DataGenerator.GSON, cache, advancement.deconstruct().serializeToJson(), path1);
                } catch (IOException ioexception) {
                    LOGGER.error("Couldn't save advancement {}", path1, ioexception);
                }
            }
        };

        advancementProvider.accept(consumer);
    }

    private static Path getPath(Path pathIn, Advancement advancementIn) {
        return pathIn.resolve("data/" + advancementIn.getId().getNamespace() + "/advancements/" + advancementIn.getId().getPath() + ".json");
    }
}