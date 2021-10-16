package mod.chiselsandbits.events;

import mod.chiselsandbits.api.plugin.IChiselsAndBitsPlugin;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.plugin.PluginManger;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FMLCommonSetupEventHandler
{

    @SubscribeEvent
    public static void onCommonSetup(
      final FMLCommonSetupEvent event)
    {
        PluginManger.getInstance().run(IChiselsAndBitsPlugin::onCommonSetup);
    }
}