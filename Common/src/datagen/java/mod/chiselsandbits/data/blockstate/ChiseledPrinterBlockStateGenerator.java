package mod.chiselsandbits.data.blockstate;

import com.google.common.collect.Maps;
import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.block.ChiseledPrinterBlock;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiseledPrinterBlockStateGenerator implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ChiseledPrinterBlockStateGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ChiseledPrinterBlockStateGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(final @NotNull HashCache cache) throws IOException
    {
        actOnBlock(cache, ModBlocks.CHISELED_PRINTER.get());
    }

    public void actOnBlock(final HashCache cache, final Block block) throws IOException
    {
        final Map<String, BlockstateVariantJson> variants = Maps.newHashMap();

        ChiseledPrinterBlock.FACING.getPossibleValues().forEach(dir -> {
            final String variantKey = String.format("%s=%s", ChiseledPrinterBlock.FACING.getName(), dir);
            String modelFile = Constants.DataGenerator.CHISELED_PRINTER_MODEL.toString();
            final BlockstateModelJson model = new BlockstateModelJson(modelFile, 0, (int) dir.getOpposite().toYRot());
            variants.put(variantKey, new BlockstateVariantJson(model));
        });

        final BlockstateJson blockstateJson = new BlockstateJson(variants);
        final Path blockstateFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.BLOCKSTATE_DIR);
        final Path blockstatePath = blockstateFolder.resolve(Objects.requireNonNull(block.getRegistryName()).getPath() + ".json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, blockstateJson.serialize(), blockstatePath);
    }

    @Override
    public @NotNull String getName()
    {
        return "Chiseled printer blockstate generator";
    }
}
