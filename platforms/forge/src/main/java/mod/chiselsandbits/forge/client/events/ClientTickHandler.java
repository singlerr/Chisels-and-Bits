package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.client.logic.ToolNameHighlightTickHandler;
import mod.chiselsandbits.keys.KeyBindingManager;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTickHandler
{
    private static final Logger LOGGER = LogManager.getLogger();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void handleClientTickForMagnifyingGlass(final TickEvent.ClientTickEvent event)
    {
        ToolNameHighlightTickHandler.handleClientTickForMagnifyingGlass();
    }

    @SubscribeEvent
    public static void handleClientTickForKeybindings(final TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START)
            return;

        KeyBindingManager.getInstance().handleKeyPresses();
    }
}
