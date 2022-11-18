package mod.chiselsandbits.forge.data.model;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
public class BlockBitItemModelGenerator extends ItemModelProvider implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(true, new BlockBitItemModelGenerator(event.getGenerator(), event.getExistingFileHelper()));
    }

    public BlockBitItemModelGenerator(final DataGenerator generator, final ExistingFileHelper existingFileHelper)
    {
        super(generator, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        actOnBlockWithLoader(new ResourceLocation(Constants.MOD_ID, "bit"), ModItems.ITEM_BLOCK_BIT.get());
    }

    public void actOnBlockWithLoader(final ResourceLocation loader, final Item item)
    {
        getBuilder(
                Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).getPath()
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
