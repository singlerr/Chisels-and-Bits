package mod.chiselsandbits.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.InventoryBackup;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketUndo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class UndoTracker
{

	private final static UndoTracker instance = new UndoTracker();

	public static UndoTracker getInstance()
	{
		return instance;
	}

	int level = -1; // the current undo level.
	boolean recording = true; // is the system currently recording undos?
	boolean grouping = false; // is a group active?
	boolean hasCreatedGroup = false; // has a new item already been added?

	private final List<UndoStep> undoLevels = new ArrayList<UndoStep>();

	// errors produced by operations are accumulated for display.
	private final Set<String> errors = new HashSet<String>();

	/**
	 * capture stack trace from whoever opened the undo group, for display
	 * later.
	 */
	private RuntimeException groupStarted;

	public void add(
			final World world,
			final BlockPos pos,
			final VoxelBlobStateReference before,
			final VoxelBlobStateReference after )
	{
		// servers don't track undo's
		if ( world.isRemote && recording )
		{
			if ( undoLevels.size() > level && !undoLevels.isEmpty() )
			{
				final int end = Math.max( -1, level );
				for ( int x = undoLevels.size() - 1; x > end; --x )
				{
					undoLevels.remove( x );
				}
			}

			if ( undoLevels.size() > ChiselsAndBits.getConfig().maxUndoLevel )
			{
				undoLevels.remove( 0 );
			}

			if ( grouping && hasCreatedGroup )
			{
				final UndoStep current = undoLevels.get( undoLevels.size() - 1 );
				final UndoStep newest = new UndoStep( world.provider.getDimensionId(), pos, before, after );
				undoLevels.set( undoLevels.size() - 1, newest );
				newest.next = current;
				return;
			}

			undoLevels.add( new UndoStep( world.provider.getDimensionId(), pos, before, after ) );
			hasCreatedGroup = true;
			level = undoLevels.size() - 1;
		}
	}

	public void undo()
	{
		if ( level > -1 )
		{
			final UndoStep step = undoLevels.get( level );
			final EntityPlayer player = ClientSide.instance.getPlayer();

			if ( correctWorld( player, step ) )
			{
				final InventoryBackup backup = new InventoryBackup( player.inventory );
				final boolean result = actions( player, step, true, true );
				backup.rollback();

				if ( result )
				{
					if ( actions( player, step, true, false ) )
					{
						level--;
					}
				}

				displayError();
			}
		}
		else
		{
			ClientSide.instance.getPlayer().addChatMessage( new ChatComponentTranslation( "mod.chiselsandbits.result.nothing_to_undo" ) );
		}
	}

	public void redo()
	{
		if ( level + 1 < undoLevels.size() )
		{
			final UndoStep step = undoLevels.get( level + 1 );
			final EntityPlayer player = ClientSide.instance.getPlayer();

			if ( correctWorld( player, step ) )
			{
				final InventoryBackup backup = new InventoryBackup( player.inventory );
				final boolean result = actions( player, step, false, true );
				backup.rollback();

				if ( result )
				{
					if ( actions( player, step, false, false ) )
					{
						level++;
					}
				}

				displayError();
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
		undoLevels.clear();
	}

	private boolean actions(
			final EntityPlayer player,
			UndoStep step,
			final boolean backwards,
			final boolean test )
	{
		boolean done = false;

		while ( step != null && trigger( player, test, step.pos, backwards ? step.after : step.before, backwards ? step.before : step.after ) )
		{
			step = step.next;
			if ( step == null )
			{
				done = true;
			}
		}

		return done;
	}

	private boolean trigger(
			final EntityPlayer player,
			final boolean test,
			final BlockPos pos,
			final VoxelBlobStateReference before,
			final VoxelBlobStateReference after )
	{
		if ( test )
		{
			return canApply( player, pos, before, after );
		}

		return apply( player, pos, before, after );
	}

	private boolean correctWorld(
			final EntityPlayer player,
			final UndoStep step )
	{
		return player.dimension == step.dimensionId;
	}

	/**
	 * This method modifies the player, make sure you backup and restore the
	 * players inventory prior to doing this...
	 *
	 * @param pos
	 * @param before
	 * @param after
	 * @return
	 */
	private boolean canApply(
			final EntityPlayer player,
			final BlockPos pos,
			final VoxelBlobStateReference before,
			final VoxelBlobStateReference after )
	{
		final PacketUndo packet = new PacketUndo( pos, before, after );
		if ( packet.canPreformAction( player ) )
		{
			return true;
		}

		return false;
	}

	private boolean apply(
			final EntityPlayer player,
			final BlockPos pos,
			final VoxelBlobStateReference before,
			final VoxelBlobStateReference after )
	{
		try
		{
			recording = false;
			final PacketUndo packet = new PacketUndo( pos, before, after );
			if ( packet.preformAction( player ) )
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

	public void beginGroup(
			final EntityPlayer player )
	{
		if ( grouping )
		{
			throw new RuntimeException( "Opening a new group, previous group already started.", groupStarted );
		}

		// capture stack...
		groupStarted = new RuntimeException( "Group was not closed properly." );
		groupStarted.fillInStackTrace();

		grouping = true;
		hasCreatedGroup = false;
	}

	public void endGroup(
			final EntityPlayer player )
	{
		if ( !grouping )
		{
			throw new RuntimeException( "Closing undo group, but no undogroup was started." );
		}

		groupStarted = null;
		grouping = false;
	}

	@SideOnly( Side.CLIENT )
	private void displayError()
	{
		for ( final String err : errors )
		{
			ClientSide.instance.getPlayer().addChatMessage( new ChatComponentTranslation( err ) );
		}

		errors.clear();
	}

	public void addError(
			final EntityPlayer player,
			final String string )
	{
		// servers don't care about this...
		if ( player.worldObj.isRemote )
		{
			errors.add( string );
		}
	}

}
