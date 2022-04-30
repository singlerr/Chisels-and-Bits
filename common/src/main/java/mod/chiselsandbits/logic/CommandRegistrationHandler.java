package mod.chiselsandbits.logic;

import com.mojang.brigadier.CommandDispatcher;
import mod.chiselsandbits.command.CommandManager;
import net.minecraft.commands.CommandSourceStack;

public class CommandRegistrationHandler
{

    public static void registerCommandsTo(final CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandManager.getInstance().register(dispatcher);
    }
}
