package mod.chiselsandbits.data.tags;

import mod.chiselsandbits.api.data.tag.AbstractChiselableTagGenerator;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockedChiselableTagGenerator extends AbstractChiselableTagGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new BlockedChiselableTagGenerator(event.getGenerator(), event.getExistingFileHelper()));
    }

    private BlockedChiselableTagGenerator(final DataGenerator generator, final ExistingFileHelper existingFileHelper)
    {
        super(generator, existingFileHelper, Mode.BLOCKED);
    }

    @Override
    protected void addElements(final TagAppender<Block> builder)
    {
        //Noop for now.
    }
}
