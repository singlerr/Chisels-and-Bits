package mod.chiselsandbits.network.packets;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.chiseledblock.data.BitIterator;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.client.UndoTracker;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.api.BitAccess;
import mod.chiselsandbits.helpers.ContinousChisels;
import mod.chiselsandbits.helpers.IContinuousInventory;
import mod.chiselsandbits.helpers.InventoryBackup;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class PacketUndo extends ModPacket
{

	BlockPos pos;
	VoxelBlobStateReference before;
	VoxelBlobStateReference after;

	public PacketUndo()
	{
		// walrus.
	}

	public PacketUndo(
			final BlockPos pos,
			final VoxelBlobStateReference before,
			final VoxelBlobStateReference after )
	{
		this.pos = pos;
		this.before = before;
		this.after = after;
	}

	@Override
	public void server(
			final EntityPlayerMP player )
	{
		preformAction( player );
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeBlockPos( pos );

		final byte[] bef = before.getByteArray();
		buffer.writeVarIntToBuffer( bef.length );
		buffer.writeBytes( bef );

		final byte[] aft = after.getByteArray();
		buffer.writeVarIntToBuffer( aft.length );
		buffer.writeBytes( aft );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		pos = buffer.readBlockPos();

		final int lena = buffer.readVarIntFromBuffer();
		final byte[] ta = new byte[lena];
		buffer.readBytes( ta );

		final int lenb = buffer.readVarIntFromBuffer();
		final byte[] tb = new byte[lenb];
		buffer.readBytes( tb );

		before = new VoxelBlobStateReference( ta, 0 );
		after = new VoxelBlobStateReference( tb, 0 );
	}

	/**
	 * This method modifies the player, make sure you backup and restore the
	 * players inventory prior to doing this...
	 *
	 * @param player
	 * @return
	 */
	public boolean canPreformAction(
			final EntityPlayer player )
	{
		if ( inRange( player, pos ) )
		{
			return apply( player, true );
		}

		return false;
	}

	public boolean preformAction(
			final EntityPlayer player )
	{
		if ( inRange( player, pos ) )
		{
			return apply( player, false );
		}

		return false;
	}

	private boolean apply(
			final EntityPlayer player,
			final boolean testing )
	{
		try
		{
			final EnumFacing side = EnumFacing.UP;

			final World world = player.worldObj;
			final BitAccess ba = (BitAccess) ChiselsAndBits.getApi().getBitAccess( world, pos );

			final VoxelBlob bBefore = before.getVoxelBlob();
			final VoxelBlob bAfter = after.getVoxelBlob();

			final VoxelBlob target = ba.getNativeBlob();

			if ( target.equals( bBefore ) )
			{
				final InventoryBackup backup = new InventoryBackup( player.inventory );

				boolean successful = true;

				final IContinuousInventory selected = new ContinousChisels( player, pos, side );
				ItemStack spawnedItem = null;

				final List<BagInventory> bags = ModUtil.getBags( player );
				final List<EntityItem> spawnlist = new ArrayList<EntityItem>();

				final BitIterator bi = new BitIterator();
				while ( bi.hasNext() )
				{
					final int inBefore = bi.getNext( bBefore );
					final int inAfter = bi.getNext( bAfter );

					if ( inBefore != inAfter )
					{
						if ( inAfter == 0 )
						{
							if ( selected.isValid() )
							{
								spawnedItem = ItemChisel.chiselBlock( selected, player, target, world, pos, side, bi.x, bi.y, bi.z, spawnedItem, spawnlist );
							}
							else
							{
								successful = false;
								break;
							}
						}
						else if ( inAfter != 0 )
						{
							final ItemStackSlot bit = ModUtil.findBit( player, inAfter );
							if ( ModUtil.consumeBagBit( bags, inAfter ) )
							{
								bi.setNext( target, inAfter );
							}
							else if ( bit.isValid() )
							{
								bi.setNext( target, inAfter );
								if ( !player.capabilities.isCreativeMode )
								{
									bit.consume();
								}
							}
							else
							{
								successful = false;
								break;
							}
						}
					}
				}

				if ( testing )
				{
					if ( !successful )
					{
						UndoTracker.getInstance().addError( player, "mod.chiselsandbits.result.missing_bits" );
					}

					return successful;
				}

				if ( successful )
				{
					ba.commitChanges( true );
					for ( final EntityItem ei : spawnlist )
					{
						ModUtil.feedPlayer( player.worldObj, player, ei );
						ItemBitBag.cleanupInventory( player, ei.getEntityItem() );
					}

					return true;
				}
				else
				{
					backup.rollback();
					return false;
				}
			}
		}
		catch ( final CannotBeChiseled e )
		{
			// error message below.
		}

		if ( testing )
		{
			UndoTracker.getInstance().addError( player, "mod.chiselsandbits.result.has_changed" );
			return false;
		}

		return false;

	}

	private boolean inRange(
			final EntityPlayer player,
			final BlockPos pos )
	{
		double reach = 6;
		if ( player.capabilities.isCreativeMode )
		{
			reach = 32;
		}

		if ( player.getDistanceSq( pos ) < reach * reach )
		{
			return true;
		}

		UndoTracker.getInstance().addError( player, "mod.chiselsandbits.result.out_of_range" );
		return false;
	}

}
