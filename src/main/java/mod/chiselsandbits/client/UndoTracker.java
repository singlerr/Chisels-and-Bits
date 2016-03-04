package mod.chiselsandbits.client;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketUndo;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class UndoTracker
{

	private final static UndoTracker instance = new UndoTracker();

	public static UndoTracker getInstance()
	{
		return instance;
	}

	int level = -1;
	boolean recording = true;
	private final List<UndoStep> undos = new ArrayList<UndoStep>();

	public void add(
			final World world,
			final BlockPos pos,
			final VoxelBlobStateReference before,
			final VoxelBlobStateReference after )
	{
		// servers don't track undo's
		if ( world.isRemote && recording )
		{
			if ( undos.size() > level && !undos.isEmpty() )
			{
				final int end = Math.max( 0, level );
				for ( int x = undos.size() - 1; x > end; --x )
				{
					undos.remove( x );
				}
			}

			if ( undos.size() > ChiselsAndBits.getConfig().maxUndoLevel )
			{
				undos.remove( 0 );
			}

			undos.add( new UndoStep( world.provider.getDimensionId(), pos, before, after ) );
			level = undos.size() - 1;
		}
	}

	public void undo()
	{
		if ( level > -1 )
		{
			final UndoStep step = undos.get( level );

			if ( correctWorld( step ) )
			{
				if ( apply( step.pos, step.after, step.before ) )
				{
					level--;
				}
			}
		}
		else
		{
			ClientSide.instance.getPlayer().addChatMessage( new ChatComponentTranslation( "mod.chiselsandbits.result.nothing_to_undo" ) );
		}
	}

	public void redo()
	{
		if ( level + 1 < undos.size() )
		{
			final UndoStep step = undos.get( level + 1 );

			if ( correctWorld( step ) )
			{
				if ( apply( step.pos, step.before, step.after ) )
				{
					level++;
				}
			}
		}
		else
		{
			ClientSide.instance.getPlayer().addChatMessage( new ChatComponentTranslation( "mod.chiselsandbits.result.nothing_to_redo" ) );
		}
	}

	public void clear()
	{
		level = -1;
		undos.clear();
	}

	private boolean correctWorld(
			final UndoStep step )
	{
		return ClientSide.instance.getPlayer().dimension == step.dimensionId;
	}

	private boolean apply(
			final BlockPos pos,
			final VoxelBlobStateReference before,
			final VoxelBlobStateReference after )
	{
		try
		{
			recording = false;
			final PacketUndo packet = new PacketUndo( pos, before, after );
			if ( packet.preformAction( ClientSide.instance.getPlayer() ) )
			{
				NetworkRouter.instance.sendToServer( packet );
				return true;
			}

			return false;
		}
		finally
		{
			recording = true;
		}
	}

}
