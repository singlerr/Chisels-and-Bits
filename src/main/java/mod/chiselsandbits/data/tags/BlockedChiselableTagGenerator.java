package mod.chiselsandbits.data.tags;

import com.google.common.collect.Lists;
import mod.chiselsandbits.api.data.tag.AbstractChiselableTagGenerator;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockedChiselableTagGenerator extends AbstractChiselableTagGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new BlockedChiselableTagGenerator(event.getGenerator()));
    }

    private BlockedChiselableTagGenerator(final DataGenerator generator)
    {
        super(generator, Mode.BLOCKED, Lists.newArrayList());
    }
}
