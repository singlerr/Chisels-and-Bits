package mod.chiselsandbits.forge.data.tag;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;
import net.minecraftforge.common.data.ForgeItemTagsProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TagGeneratorEventHandler
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        ForgeBlockTagsProvider forgeBlockTags = new ForgeBlockTagsProvider(event.getGenerator(), event.getExistingFileHelper());
        event.getGenerator().addProvider(forgeBlockTags);
        event.getGenerator().addProvider(new ForgeItemTagsProvider(event.getGenerator(), forgeBlockTags, event.getExistingFileHelper()));

        ModBlockTagGenerator modBlockTags = new ModBlockTagGenerator(event.getGenerator(), event.getExistingFileHelper());
        event.getGenerator().addProvider(modBlockTags);
        event.getGenerator().addProvider(new ModItemTagGenerator(event.getGenerator(), modBlockTags, event.getExistingFileHelper()));
    }
}
