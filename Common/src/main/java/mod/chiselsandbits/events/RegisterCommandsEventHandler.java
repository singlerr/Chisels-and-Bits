package mod.chiselsandbits.events;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.command.CommandManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RegisterCommandsEventHandler
{
    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event)
    {
        CommandManager.getInstance().register(event.getDispatcher());
    }
}
