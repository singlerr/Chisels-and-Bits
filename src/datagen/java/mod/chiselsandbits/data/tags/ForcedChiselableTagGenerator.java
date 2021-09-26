package mod.chiselsandbits.data.tags;

import com.google.common.collect.Lists;
import mod.chiselsandbits.api.data.tag.AbstractChiselableTagGenerator;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import mod.chiselsandbits.api.data.tag.AbstractChiselableTagGenerator.Mode;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForcedChiselableTagGenerator extends AbstractChiselableTagGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ForcedChiselableTagGenerator(event.getGenerator()));
    }

    private ForcedChiselableTagGenerator(final DataGenerator generator)
    {
        super(generator,
          Mode.FORCED,
          Lists.newArrayList(Blocks.GRASS_BLOCK));
    }
}
