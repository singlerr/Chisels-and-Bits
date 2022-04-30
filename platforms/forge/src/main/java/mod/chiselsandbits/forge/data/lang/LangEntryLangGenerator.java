package mod.chiselsandbits.forge.data.lang;

import com.google.gson.JsonObject;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LangEntryLangGenerator implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new LangEntryLangGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private LangEntryLangGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(@NotNull final HashCache cache) throws IOException
    {
        final List<String> langKeys = Arrays.stream(LocalStrings.values()).map(LocalStrings::toString).collect(Collectors.toList());
        Collections.sort(langKeys);
        final JsonObject returnValue = new JsonObject();

        for (String langKey : langKeys)
        {
            returnValue.addProperty(langKey, "");
        }

        final Path configLangFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.CONFIG_LANG_DIR);
        final Path langPath = configLangFolder.resolve("localstrings.json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, returnValue, langPath);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Chiseled config lang generator";
    }
}
