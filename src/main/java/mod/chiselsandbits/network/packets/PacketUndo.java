package mod.chiselsandbits.network.packets;

import java.util.List;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.chiseledblock.data.BitIterator;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.client.UndoTracker;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.api.BitAccess;
import mod.chiselsandbits.helpers.ActingPlayer;
import mod.chiselsandbits.helpers.ContinousChisels;
import mod.chiselsandbits.helpers.IContinuousInventory;
import mod.chiselsandbits.helpers.InfiniteBitStorage;
import mod.chiselsandbits.helpers.InventoryBackup;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PacketUndo extends ModPacket
{

	public boolean verbose = true;

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
		verbose = false;
		preformAction( ActingPlayer.actingAs( player, EnumHand.MAIN_HAND ), true );
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

	public boolean preformAction(
			final ActingPlayer player,
			final boolean spawnItemsAndCommitWorldChanges )
	{
		if ( inRange( player, pos ) )
		{
			return apply( player, spawnItemsAndCommitWorldChanges );
		}
		else
			addError( player, "mod.chiselsandbits.result.out_of_range" );

		return false;
	}

	private boolean apply(
			final ActingPlayer player,
			final boolean spawnItemsAndCommitWorldChanges )
	{
		try
		{
			final EnumFacing side = EnumFacing.UP;

			final World world = player.getWorld();
			final BitAccess ba = (BitAccess) ChiselsAndBits.getApi().getBitAccess( world, pos );

			final VoxelBlob bBefore = before.getVoxelBlob();
			final VoxelBlob bAfter = after.getVoxelBlob();

			final VoxelBlob target = ba.getNativeBlob();

			if ( target.equals( bBefore ) )
			{
				// if something horrible goes wrong in a single block change we
				// can roll it back, but it shouldn't happen since its already
				// been approved as possible.
				final InventoryBackup backup = new InventoryBackup( player.getInventory() );

				boolean successful = true;

				final IContinuousInventory selected = new ContinousChisels( player, pos, side );
				InfiniteBitStorage infiniteStorage = new InfiniteBitStorage();
				final List<BagInventory> bags = ModUtil.getBags( player );

				boolean noMoreChisels = false;
				String whichBit = "NO_SUCH_BIT";

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
								ItemChisel.chiselBlock( selected, player, target, world, pos, side, bi.x, bi.y, bi.z, infiniteStorage );
							}
							else
							{
								noMoreChisels = true;
								successful = false;
								break;
							}
						}
						else if ( inAfter != 0 )
						{
							if ( inBefore != 0 )
							{
								if ( selected.isValid() )
								{
									ItemChisel.chiselBlock( selected, player, target, world, pos, side, bi.x, bi.y, bi.z, infiniteStorage );
								}
								else
								{
									successful = false;
									break;
								}
							}

							if ( infiniteStorage.dec( inAfter ) )
							{
								bi.setNext( target, inAfter );
							}
							else if ( ModUtil.consumeBagBit( bags, inAfter, 1 ) == 1 )
							{
								bi.setNext( target, inAfter );
							}
							else
							{
								final ItemStackSlot bit = ModUtil.findBit( player, pos, inAfter );
								if ( bit.isValid() )
								{
									bi.setNext( target, inAfter );
									if ( !player.isCreative() )
									{
										bit.consume();
									}
								}
								else
								{
									if ( infiniteStorage.chiselBlock( inAfter, player, selected ) )
									{
										infiniteStorage.dec( inAfter );
										bi.setNext( target, inAfter );
									}
									else
									{
										try
										{
											whichBit = ChiselsAndBits.getApi().getBitItem( ModUtil.getStateById( inAfter ) ).getDisplayName();
										}
										catch ( InvalidBitItem e )
										{
											// hmm...
										}

										successful = false;
										break;
									}
								}
							}
						}
					}
				}

				if ( successful )
				{
					if ( spawnItemsAndCommitWorldChanges )
					{
						ba.commitChanges( true );
						infiniteStorage.give( player );
					}

					return true;
				}
				else
				{
					backup.rollback();

					if ( noMoreChisels )
						addError( player, "mod.chiselsandbits.result.missing_chisels" );
					else if ( whichBit != null )
						addError( player, "mod.chiselsandbits.result.missing_bits", whichBit );

					return false;
				}
			}
		}
		catch ( final CannotBeChiseled e )
		{
			// error message below.
		}

		addError( player, "mod.chiselsandbits.result.has_changed" );
		return false;
	}

	public boolean inRange(
			final ActingPlayer player,
			final BlockPos pos )
	{
		if ( player.isReal() )
		{
			return true;
		}

		double reach = 6;
		if ( player.isCreative() )
		{
			reach = 32;
		}

		if ( player.getPlayer().getDistanceSq( pos ) < reach * reach )
		{
			return true;
		}

		return false;
	}

	private void addError(
			final ActingPlayer player,
			final String string,
			String... args )
	{
		if ( verbose )
		{
			UndoTracker.getInstance().addError( player, string, args );
		}
	}

}
