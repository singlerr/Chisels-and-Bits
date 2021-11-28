package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.icon.IconManager;
import mod.chiselsandbits.client.reloading.ClientResourceReloadingManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class IconInitializationManager
{
    @SubscribeEvent
    public static void onBlockColorHandler(final ColorHandlerEvent.Block event)
    {
        //We use this event since this is virtually the only time we can init the IconManager and have it load the custom atlas.
        //Guard for doing stupid shit when data gen is running :D
        if (Minecraft.getInstance() != null)
        {
            ClientResourceReloadingManager.setup();
            IconManager.getInstance().initialize();
        }
    }
}
