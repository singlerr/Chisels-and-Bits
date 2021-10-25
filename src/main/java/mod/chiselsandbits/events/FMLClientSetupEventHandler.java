package mod.chiselsandbits.events;

import mod.chiselsandbits.api.plugin.IChiselsAndBitsPlugin;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.plugin.PluginManger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FMLClientSetupEventHandler
{

    @SubscribeEvent
    public static void onCommonSetup(
      final FMLCommonSetupEvent event)
    {
        PluginManger.getInstance().run(IChiselsAndBitsPlugin::onClientSetup);
    }
}