package mod.chiselsandbits.data.lang;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import com.ldtteam.datagenerators.lang.LangJson;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.config.AbstractConfiguration;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigLangGenerator implements IDataProvider
{
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ConfigLangGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ConfigLangGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void act(final DirectoryCache cache) throws IOException
    {
        final List<String> langKeys = new ArrayList<>(AbstractConfiguration.LANG_KEYS);
        Collections.sort(langKeys);
        final JsonObject returnValue = new JsonObject();

        for (String langKey : langKeys)
        {
            returnValue.addProperty(langKey, "");
        }

        final Path configLangFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.CONFIG_LANG_DIR);
        final Path langPath = configLangFolder.resolve("config.json");

        IDataProvider.save(Constants.DataGenerator.GSON, cache, returnValue, langPath);
    }

    @Override
    public String getName()
    {
        return "Chiseled config lang generator";
    }
}
