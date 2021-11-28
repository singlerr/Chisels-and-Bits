package mod.chiselsandbits.forge.events;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.logic.CommandRegistrationHandler;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RegisterCommandsEventHandler
{
    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event)
    {
        CommandRegistrationHandler.registerCommandsTo(event.getDispatcher());
    }
}
