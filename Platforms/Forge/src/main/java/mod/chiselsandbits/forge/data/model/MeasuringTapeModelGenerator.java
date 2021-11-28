package mod.chiselsandbits.forge.data.model;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MeasuringTapeModelGenerator extends ItemModelProvider
{
    private MeasuringTapeModelGenerator(final DataGenerator generator, final ExistingFileHelper existingFileHelper)
    {
        super(generator, Constants.MOD_ID, existingFileHelper);
    }

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new MeasuringTapeModelGenerator(event.getGenerator(), event.getExistingFileHelper()));
    }

    @Override
    protected void registerModels()
    {
        getBuilder("measuring_tape")
          .parent(new ModelFile.UncheckedModelFile("item/generated"))
          .texture("layer0", new ResourceLocation(Constants.MOD_ID, "items/tape_measure"))
          .transforms()
          .transform(ModelBuilder.Perspective.THIRDPERSON_RIGHT)
          .rotation(-80, 260, -40)
          .translation(-1, -2, 2.5f)
          .scale(0.9f, 0.9f, 0.9f)
          .end()
          .transform(ModelBuilder.Perspective.THIRDPERSON_LEFT)
          .rotation(-80, -280, 40)
          .translation(-1, -2, 2.5f)
          .scale(0.9f, 0.9f, 0.9f)
          .end()
          .transform(ModelBuilder.Perspective.FIRSTPERSON_RIGHT)
          .rotation(0,-90,25)
          .translation(1.13f, 3.2f, 1.13f)
          .scale(0.68f,0.68f,0.68f)
          .end()
          .transform(ModelBuilder.Perspective.FIRSTPERSON_LEFT)
          .rotation(0,90,-25)
          .translation(1.13f, 3.2f, 1.13f)
          .scale(0.68f,0.68f,0.68f)
          .end()
          .end()
          .override()
          .predicate(new ResourceLocation(Constants.MOD_ID, "is_measuring"), 1)
          .model(new ModelFile.UncheckedModelFile(new ResourceLocation(Constants.MOD_ID, "item/measuring_tape_is_measuring")))
          .end();

        getBuilder("measuring_tape_is_measuring")
          .parent(getBuilder("measuring_tape"))
          .texture("layer0", "items/tape_measure_is_measuring");
    }
}
