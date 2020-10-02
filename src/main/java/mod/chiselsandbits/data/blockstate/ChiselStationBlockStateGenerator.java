package mod.chiselsandbits.data.blockstate;

import com.google.common.collect.Maps;
import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.station.ChiselStationBlock;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiselStationBlockStateGenerator implements IDataProvider
{
    public static final int INT = 90;

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ChiselStationBlockStateGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ChiselStationBlockStateGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void act(final DirectoryCache cache) throws IOException
    {
        actOnBlock(cache, ModBlocks.CHISEL_STATION_BLOCK.get());
    }

    public void actOnBlock(final DirectoryCache cache, final Block block) throws IOException
    {
        final Map<String, BlockstateVariantJson> variants = Maps.newHashMap();

        ChiselStationBlock.FACING.getAllowedValues().forEach(dir -> {
            final String variantKey = String.format("%s=%s", ChiselStationBlock.FACING.getName(), dir);
            String modelFile = Constants.DataGenerator.CHISEL_STATION_MODEL.toString();
            final BlockstateModelJson model = new BlockstateModelJson(modelFile, 0, (int) dir.getHorizontalAngle()- INT);
            variants.put(variantKey, new BlockstateVariantJson(model));
        });

        final BlockstateJson blockstateJson = new BlockstateJson(variants);
        final Path blockstateFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.BLOCKSTATE_DIR);
        final Path blockstatePath = blockstateFolder.resolve(block.getRegistryName().getPath() + ".json");

        IDataProvider.save(Constants.DataGenerator.GSON, cache, blockstateJson.serialize(), blockstatePath);
    }

    @Override
    public String getName()
    {
        return "ChiselStation blockstate generator";
    }
}
