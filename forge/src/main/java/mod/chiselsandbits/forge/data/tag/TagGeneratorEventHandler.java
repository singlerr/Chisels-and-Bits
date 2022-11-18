package mod.chiselsandbits.forge.data.tag;

import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;
import net.minecraftforge.common.data.ForgeItemTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TagGeneratorEventHandler
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        ForgeBlockTagsProvider forgeBlockTags = new ForgeBlockTagsProvider(event.getGenerator(), event.getExistingFileHelper());
        event.getGenerator().addProvider(true, forgeBlockTags);
        event.getGenerator().addProvider(true, new ForgeItemTagsProvider(event.getGenerator(), forgeBlockTags, event.getExistingFileHelper()));

        ModBlockTagGenerator modBlockTags = new ModBlockTagGenerator(event.getGenerator(), event.getExistingFileHelper());
        event.getGenerator().addProvider(true, modBlockTags);
        event.getGenerator().addProvider(true, new ModItemTagGenerator(event.getGenerator(), modBlockTags, event.getExistingFileHelper()));
    }
}
