package mod.chiselsandbits.forge.data.model;

import com.communi.suggestu.scena.core.registries.deferred.IRegistryObject;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiselBlockItemModelGenerator extends ItemModelProvider implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(true, new ChiselBlockItemModelGenerator(event.getGenerator(), event.getExistingFileHelper()));
    }

    public ChiselBlockItemModelGenerator(final DataGenerator generator, final ExistingFileHelper existingFileHelper)
    {
        super(generator, Constants.MOD_ID, existingFileHelper);
    }
    @Override
    protected void registerModels()
    {
        ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values()
          .stream()
          .map(IRegistryObject::get)
          .forEach(block -> actOnBlockWithLoader(new ResourceLocation(Constants.MOD_ID, "chiseled_block"), block));
    }

    public void actOnBlockWithLoader(final ResourceLocation loader, final Block block)
    {
        getBuilder(
                Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getPath()
        )
          .parent(getExistingFile(new ResourceLocation("item/generated")))
          .customLoader((itemModelBuilder, existingFileHelper) -> new CustomLoaderBuilder<>(loader, itemModelBuilder, existingFileHelper)
          {
          });
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Chisel block item model generator";
    }
}
