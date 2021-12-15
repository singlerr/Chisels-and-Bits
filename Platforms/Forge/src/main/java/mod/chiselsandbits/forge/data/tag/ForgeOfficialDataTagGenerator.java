package mod.chiselsandbits.forge.data.tag;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;
import net.minecraftforge.common.data.ForgeItemTagsProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeOfficialDataTagGenerator
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        ForgeBlockTagsProvider blockTags = new ForgeBlockTagsProvider(event.getGenerator(), event.getExistingFileHelper());
        event.getGenerator().addProvider(blockTags);
        event.getGenerator().addProvider(new ForgeItemTagsProvider(event.getGenerator(), blockTags, event.getExistingFileHelper()));
    }
}
