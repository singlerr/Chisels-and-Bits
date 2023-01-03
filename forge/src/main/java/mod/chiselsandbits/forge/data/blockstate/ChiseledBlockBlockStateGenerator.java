package mod.chiselsandbits.forge.data.blockstate;

import com.communi.suggestu.scena.core.registries.deferred.IRegistryObject;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiseledBlockBlockStateGenerator extends BlockStateProvider implements DataProvider
{

    private static final ResourceLocation CHISELED_BLOCK_MODEL = new ResourceLocation(Constants.MOD_ID, "block/chiseled");

    public ChiseledBlockBlockStateGenerator(final DataGenerator gen, final ExistingFileHelper exFileHelper)
    {
        super(gen.getPackOutput(), Constants.MOD_ID, exFileHelper);
    }

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(true, new ChiseledBlockBlockStateGenerator(event.getGenerator(), event.getExistingFileHelper()));
    }

    @Override
    protected void registerStatesAndModels()
    {
        ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values()
          .stream()
          .map(IRegistryObject::get)
          .forEach(this::actOnBlock);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Chiseled block blockstate generator";
    }

    public void actOnBlock(final Block block)
    {
        getVariantBuilder(block)
          .forAllStates(blockState -> ConfiguredModel.builder()
            .modelFile(models().getExistingFile(CHISELED_BLOCK_MODEL))
            .build());
    }
}
