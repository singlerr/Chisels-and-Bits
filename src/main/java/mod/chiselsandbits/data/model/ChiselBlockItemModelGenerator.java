package mod.chiselsandbits.data.model;

import com.ldtteam.datagenerators.models.item.ItemModelJson;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.MaterialType;
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
        for (MaterialType materialType : ModBlocks.VALID_CHISEL_MATERIALS)
        {
            final RegistryObject<BlockChiseled> blockChiseledRegistryObject = ModBlocks.getMaterialToBlockConversions().get(materialType.getType());
            BlockChiseled blockChiseled = blockChiseledRegistryObject.get();
            actOnBlockWithLoader(cache, blockChiseled, new ResourceLocation(Constants.MOD_ID, "chiseled_block"), materialType);
        }

        actOnBlockWithParent(cache, ModBlocks.CHISEL_PRINTER_BLOCK.get(), Constants.DataGenerator.CHISEL_PRINTER_MODEL);
    }

    @Override
    public String getName()
    {
        return "Chisel block item model generator";
    }

    public void actOnBlockWithParent(final DirectoryCache cache, final Block block, final ResourceLocation parent) throws IOException
    {
        final ItemModelJson json = new ItemModelJson();
        json.setParent(parent.toString());

        saveBlockJson(cache, block, json, Objects.requireNonNull(block.getRegistryName()).getPath());
    }

    public void actOnBlockWithLoader(final DirectoryCache cache, final Block block, final ResourceLocation loader, final MaterialType materialType) throws IOException
    {
        final ItemModelJson json = new ItemModelJson();
        json.setParent("item/generated");
        json.setLoader(loader.toString());

        saveBlockJson(cache, block, json, "chiseled" + materialType.getName());
    }

    private void saveBlockJson(final DirectoryCache cache, final Block block, final ItemModelJson json, final String name) throws IOException
    {
        final Path itemModelFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.ITEM_MODEL_DIR);
        final Path itemModelPath = itemModelFolder.resolve(name + ".json");

        IDataProvider.save(Constants.DataGenerator.GSON, cache, json.serialize(), itemModelPath);
    }
}
