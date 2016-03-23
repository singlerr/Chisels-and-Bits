package mod.chiselsandbits.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/**
 * This is a WIP, and will eventually be incorporated into a gameplay element.
 */
public class Share extends CommandBase
{
	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}

	@Override
	public String getCommandName()
	{
		return "c&b.share";
	}

	@Override
	public String getCommandUsage(
			final ICommandSender sender )
	{
		return "chiselsandbits.commands.share.usage";
	}

	BlockPos start;
	BlockPos end;

	@Override
	public void processCommand(
			final ICommandSender sender,
			final String[] args ) throws CommandException
	{
		if ( args.length > 0 && args[0].equals( "start" ) )
		{
			start = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
			sender.addChatMessage( new ChatComponentText( "Start Pos Set" ) );

			if ( start == null )
			{
				start = Minecraft.getMinecraft().thePlayer.getPosition();
			}
		}
		else if ( args.length > 0 && args[0].equals( "end" ) )
		{
			end = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
			sender.addChatMessage( new ChatComponentText( "End Pos Set" ) );

			if ( end == null )
			{
				end = Minecraft.getMinecraft().thePlayer.getPosition();
			}
		}
		else if ( start == null )
		{
			sender.addChatMessage( new ChatComponentText( "Start Pos Not Set Yet, use argument 'start'." ) );
		}
		else if ( end == null )
		{
			sender.addChatMessage( new ChatComponentText( "End Pos Not Set Yet, use argument 'end'." ) );
		}
		else if ( start != null && end != null )
		{
			final World clientWorld = Minecraft.getMinecraft().theWorld;
			new ShareGenerator( clientWorld, start, end );
		}
	}

}
