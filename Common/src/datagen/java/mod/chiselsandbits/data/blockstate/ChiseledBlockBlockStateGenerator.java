package mod.chiselsandbits.data.blockstate;

import com.google.common.collect.Maps;
import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.materials.MaterialManager;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiseledBlockBlockStateGenerator implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ChiseledBlockBlockStateGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ChiseledBlockBlockStateGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(@NotNull final HashCache cache) throws IOException
    {
        for (Map.Entry<Material, RegistryObject<ChiseledBlock>> entry : ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.entrySet())
        {
            Material material = entry.getKey();
            RegistryObject<ChiseledBlock> chiseledBlockRegistryObject = entry.getValue();
            ChiseledBlock blockChiseled = chiseledBlockRegistryObject.get();
            actOnBlock(cache, blockChiseled, MaterialManager.getInstance().getMaterialNames().get(material));
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Chiseled block blockstate generator";
    }

    public void actOnBlock(final HashCache cache, final Block block, final String materialName) throws IOException
    {
        final Map<String, BlockstateVariantJson> variants = Maps.newHashMap();

        block.getStateDefinition().getProperties().forEach(property -> property.getPossibleValues().forEach(value -> {
            final String variantKey = String.format("%s=%s", property.getName(), value);
            String modelFile = Constants.DataGenerator.CHISELED_BLOCK_MODEL.toString();
            final BlockstateModelJson model = new BlockstateModelJson(modelFile, 0, 0);
            variants.put(variantKey, new BlockstateVariantJson(model));
        }));

        if (block.getStateDefinition().getProperties().isEmpty()) {
            final String variantKey = "";
            String modelFile = Constants.DataGenerator.CHISELED_BLOCK_MODEL.toString();
            final BlockstateModelJson model = new BlockstateModelJson(modelFile, 0, 0);
            variants.put(variantKey, new BlockstateVariantJson(model));
        }

        final BlockstateJson blockstateJson = new BlockstateJson(variants);
        final Path blockstateFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.BLOCKSTATE_DIR);
        final Path blockstatePath = blockstateFolder.resolve("chiseled" +
                                                               materialName
                                                               + ".json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, blockstateJson.serialize(), blockstatePath);
    }
}
