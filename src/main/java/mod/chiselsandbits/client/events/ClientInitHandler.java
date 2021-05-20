package mod.chiselsandbits.client.events;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.client.screens.BitBagScreen;
import mod.chiselsandbits.registrars.ModContainerTypes;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInitHandler
{

    @SubscribeEvent
    public static void onFMLClientSetup(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            ScreenManager.registerFactory(
              ModContainerTypes.BIT_BAG.get(),
              BitBagScreen::new
            );
        });
    }
}
