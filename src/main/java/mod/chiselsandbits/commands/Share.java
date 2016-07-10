package mod.chiselsandbits.commands;

import javax.swing.JFileChooser;

import mod.chiselsandbits.share.ShareGenerator;
import mod.chiselsandbits.share.output.IShareOutput;
import mod.chiselsandbits.share.output.LocalClipboard;
import mod.chiselsandbits.share.output.LocalPNGFile;
import mod.chiselsandbits.share.output.LocalTextFile;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
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
	public void execute(
			final MinecraftServer server,
			final ICommandSender sender,
			final String[] args ) throws CommandException
	{
		if ( args.length > 0 && args[0].equals( "start" ) )
		{
			start = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
			sender.addChatMessage( new TextComponentString( "Start Pos Set" ) );

			if ( start == null )
			{
				start = Minecraft.getMinecraft().thePlayer.getPosition();
			}
		}
		else if ( args.length > 0 && args[0].equals( "end" ) )
		{
			end = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
			sender.addChatMessage( new TextComponentString( "End Pos Set" ) );

			if ( end == null )
			{
				end = Minecraft.getMinecraft().thePlayer.getPosition();
			}
		}
		else if ( start == null )
		{
			sender.addChatMessage( new TextComponentString( "Start Pos Not Set Yet, use argument 'start'." ) );
		}
		else if ( end == null )
		{
			sender.addChatMessage( new TextComponentString( "End Pos Not Set Yet, use argument 'end'." ) );
		}
		else if ( start != null && end != null )
		{
			final World clientWorld = Minecraft.getMinecraft().theWorld;
			IShareOutput out = new LocalClipboard();

			if ( args.length > 0 )
			{
				final JFileChooser fc = new JFileChooser();

				if ( args[0].equals( "textfile" ) || args[0].equals( "txtfile" ) || args[0].equals( "txt" ) )
				{
					if ( fc.showSaveDialog( null ) == JFileChooser.APPROVE_OPTION )
					{
						out = new LocalTextFile( fc.getSelectedFile() );
					}
				}

				if ( args[0].equals( "imagefile" ) || args[0].equals( "pngfile" ) || args[0].equals( "png" ) )
				{
					if ( fc.showSaveDialog( null ) == JFileChooser.APPROVE_OPTION )
					{
						out = new LocalPNGFile( fc.getSelectedFile() );
					}
				}
			}

			new ShareGenerator( clientWorld, start, end, out );
		}
	}

}
