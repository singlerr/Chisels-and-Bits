package mod.chiselsandbits.data.model;

import com.ldtteam.datagenerators.models.item.ItemModelJson;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiselBlockItemModelGenerator implements IDataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ChiselBlockItemModelGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ChiselBlockItemModelGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void act(final DirectoryCache cache) throws IOException
    {
        for (RegistryObject<BlockChiseled> blockChiseledRegistryObject : ModBlocks.getMaterialToBlockConversions().values())
        {
            BlockChiseled blockChiseled = blockChiseledRegistryObject.get();
            actOnBlock(cache, blockChiseled, Constants.DataGenerator.CHISELED_BLOCK_MODEL);
        }

        actOnBlock(cache, ModBlocks.CHISEL_STATION_BLOCK.get(), Constants.DataGenerator.CHISEL_STATION_MODEL);
    }

    @Override
    public String getName()
    {
        return "Chisel block item model generator";
    }

    public void actOnBlock(final DirectoryCache cache, final Block block, final ResourceLocation parent) throws IOException
    {
        final ItemModelJson json = new ItemModelJson();
        json.setParent(parent.toString());

        final Path itemModelFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.ITEM_MODEL_DIR);
        final Path itemModelPath = itemModelFolder.resolve(Objects.requireNonNull(block.getRegistryName()).getPath() + ".json");

        IDataProvider.save(Constants.DataGenerator.GSON, cache, json.serialize(), itemModelPath);
    }
}
