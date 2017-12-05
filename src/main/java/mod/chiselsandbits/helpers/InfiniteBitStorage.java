package mod.chiselsandbits.helpers;

import java.util.HashMap;
import java.util.Map.Entry;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

// ok.. so thats not entirely true, but no one cares.
public class InfiniteBitStorage
{

	private HashMap<Integer, Vec3d> spawnPoses = new HashMap<Integer, Vec3d>();
	private HashMap<Integer, Integer> storage = new HashMap<Integer, Integer>();
	private HashMap<Integer, Boolean> noBlocks = new HashMap<Integer, Boolean>();

	public void insert(
			int state,
			int qty,
			double x,
			double y,
			double z )
	{
		if ( !spawnPoses.containsKey( state ) )
		{
			spawnPoses.put( state, new Vec3d( x, y, z ) );
			action = true;
		}

		storage.put( state, get( state ) + qty );
	}

	public int attempToConsume(
			int state,
			int howManyToConsume )
	{
		int v = get( state );
		int consumeAmount = Math.min( howManyToConsume, v );

		if ( consumeAmount < 0 )
			consumeAmount = 0;

		storage.put( state, v - consumeAmount );
		return consumeAmount;
	}

	private int get(
			int state )
	{
		if ( storage.containsKey( state ) )
			return storage.get( state );

		return 0;
	}

	public boolean dec(
			int state )
	{
		int v = get( state );

		if ( v > 0 )
		{
			storage.put( state, v - 1 );
			return true;
		}

		return false;
	}

	public void give(
			ActingPlayer player )
	{
		if ( player.isReal() )
		{
			final boolean spawnBit = ChiselsAndBits.getItems().itemBlockBit != null;
			if ( spawnBit && !player.getWorld().isRemote )
			{
				World w = player.getWorld();
				for ( Entry<Integer, Integer> p : storage.entrySet() )
				{
					int total = p.getValue();
					while ( total > 0 )
					{
						int size = Math.min( 64, total );
						total -= size;

						Vec3d pos = spawnPoses.get( p.getKey() );
						final EntityItem ei = new EntityItem( w, pos.xCoord, pos.yCoord, pos.zCoord, ItemChiseledBit.createStack( p.getKey(), size, true ) );

						ModUtil.feedPlayer( player.getWorld(), player.getPlayer(), ei );
						ItemBitBag.cleanupInventory( player.getPlayer(), ei.getEntityItem() );
					}
				}
			}
		}
	}

	public boolean chiselBlock(
			int state,
			ActingPlayer player,
			IContinuousInventory chisels )
	{
		if ( noBlocks.containsKey( state ) )
			return false;

		IInventory inv = player.getInventory();

		for ( int x = 0; x < inv.getSizeInventory(); x++ )
		{
			ItemStack is = inv.getStackInSlot( x );

			IBlockState item_state = DeprecationHelper.getStateFromItem( is );

			if ( item_state != null && ModUtil.getStateId( item_state ) == state )
			{
				// I know this isn't perfect, but it seems better then just
				// throwing three inventories worth of bits on the ground
				// without warning.
				if ( player.hasBagWithRoom( state, VoxelBlob.full_size ) )
				{
					if ( chisels.useItem( state, VoxelBlob.full_size ) )
					{
						ItemStack post = is.copy();
						ModUtil.adjustStackSize( post, -1 );

						inv.setInventorySlotContents( x, post );
						BlockPos p = player.getPosition();
						this.insert( state, VoxelBlob.full_size, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5 );
						return true;
					}
					else
						player.report( ChiselErrors.NO_CHISELS );
				}
				else
					player.report( ChiselErrors.NO_BAG_SPACE );
			}
		}

		noBlocks.put( state, true );
		return false;
	}

	public int getCount(
			int blk )
	{
		return get( blk );
	}

	boolean action = false;

	public boolean hasExtracted()
	{
		return action;
	}

	public void setExtracted()
	{
		action = true;
	}

}
