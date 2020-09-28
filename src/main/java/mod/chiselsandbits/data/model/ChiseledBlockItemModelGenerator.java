package mod.chiselsandbits.data.model;

import com.google.common.collect.Maps;
import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import com.ldtteam.datagenerators.models.item.ItemModelJson;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.data.blockstate.ChiseledBlockStateGenerator;
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
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiseledBlockItemModelGenerator implements IDataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ChiseledBlockItemModelGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ChiseledBlockItemModelGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void act(final DirectoryCache cache) throws IOException
    {
        for (RegistryObject<BlockChiseled> blockChiseledRegistryObject : ModBlocks.getMaterialToBlockConversions().values())
        {
            BlockChiseled blockChiseled = blockChiseledRegistryObject.get();
            actOnBlock(cache, blockChiseled);
        }
    }

    @Override
    public String getName()
    {
        return "Chiseled block item model generator";
    }

    public void actOnBlock(final DirectoryCache cache, final Block block) throws IOException
    {
        final ItemModelJson json = new ItemModelJson();
        json.setParent(Constants.DataGenerator.CHISELED_BLOCK_MODEL.toString());

        final Path itemModelFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.ITEM_MODEL_DIR);
        final Path itemModelPath = itemModelFolder.resolve(Objects.requireNonNull(block.getRegistryName()).getPath() + ".json");

        IDataProvider.save(Constants.DataGenerator.GSON, cache, json.serialize(), itemModelPath);
    }
}
