package mod.chiselsandbits.data.model;

import com.ldtteam.datagenerators.models.item.ItemModelJson;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.materials.MaterialManager;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        for (Map.Entry<Material, RegistryObject<ChiseledBlock>> entry : ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.entrySet())
        {
            Material material = entry.getKey();
            actOnBlockWithLoader(cache, new ResourceLocation(Constants.MOD_ID, "chiseled_block"), MaterialManager.getInstance().getMaterialNames().get(material));
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Chisel block item model generator";
    }

    public void actOnBlockWithParent(final DirectoryCache cache, final Block block, final ResourceLocation parent) throws IOException
    {
        final ItemModelJson json = new ItemModelJson();
        json.setParent(parent.toString());

        saveBlockJson(cache, json, Objects.requireNonNull(block.getRegistryName()).getPath());
    }

    public void actOnBlockWithLoader(final DirectoryCache cache, final ResourceLocation loader, final String materialName) throws IOException
    {
        final ItemModelJson json = new ItemModelJson();
        json.setParent("item/generated");
        json.setLoader(loader.toString());

        saveBlockJson(cache, json, "chiseled" + materialName);
    }

    private void saveBlockJson(final DirectoryCache cache, final ItemModelJson json, final String name) throws IOException
    {
        final Path itemModelFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.ITEM_MODEL_DIR);
        final Path itemModelPath = itemModelFolder.resolve(name + ".json");

        IDataProvider.save(Constants.DataGenerator.GSON, cache, json.serialize(), itemModelPath);
    }
}
