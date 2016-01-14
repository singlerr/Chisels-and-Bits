package mod.chiselsandbits.core.api;

import java.util.HashMap;
import java.util.Map;

import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob.CommonBlock;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BitAccess implements IBitAccess
{

	private final World w;
	private final BlockPos pos;
	private final VoxelBlob blob;
	private final VoxelBlob filler;

	private final Map<Integer, IBitBrush> brushes = new HashMap<Integer, IBitBrush>();

	public BitAccess(
			final World w,
			final BlockPos pos,
			final VoxelBlob blob,
			final VoxelBlob filler )
	{
		this.w = w;
		this.pos = pos;
		this.blob = blob;
		this.filler = filler;
	}

	@Override
	public IBitBrush getBitAt(
			final int x,
			final int y,
			final int z )
	{
		final int state = blob.getSafe( x, y, z );

		IBitBrush brush = brushes.get( state );

		if ( brush == null )
		{
			brushes.put( state, brush = new BitBrush( state ) );
		}

		return brush;
	}

	@Override
	public void setBitAt(
			int x,
			int y,
			int z,
			final IBitBrush bit ) throws SpaceOccupied
	{
		int state = 0;

		if ( bit instanceof BitBrush )
		{
			state = ( (BitBrush) bit ).stateID;
		}

		// make sure that they are only 0-15
		x = x & 0xf;
		y = y & 0xf;
		z = z & 0xf;

		if ( filler.get( x, y, z ) == 0 )
		{
			blob.set( x, y, z, state );
		}
		else
		{
			throw new SpaceOccupied();
		}
	}

	@Override
	public void commitChanges()
	{
		TileEntityBlockChiseled tile = ModUtil.getChiseledTileEntity( w, pos, true );
		final CommonBlock cb = blob.mostCommonBlock();

		if ( tile == null && BlockChiseled.replaceWithChisled( w, pos, w.getBlockState( pos ), cb.ref ) )
		{
			tile = ModUtil.getChiseledTileEntity( w, pos, true );
		}

		if ( tile != null )
		{
			tile.setBlob( blob );
		}
	}

	@Override
	public ItemStack getBitsAsItem(
			final EnumFacing side,
			final ItemType type )
	{
		if ( type == null )
		{
			return null;
		}

		final CommonBlock cb = blob.mostCommonBlock();
		if ( cb.ref == 0 )
		{
			return null;
		}

		final TileEntityBlockChiseled test = new TileEntityBlockChiseled();
		test.setBlob( blob );
		final NBTTagCompound nbttagcompound = new NBTTagCompound();
		test.writeChisleData( nbttagcompound );

		final ItemStack itemstack;

		if ( type == ItemType.CHISLED_BLOCK )
		{
			final IBlockState state = Block.getStateById( cb.ref );
			final BlockChiseled blk = ChiselsAndBits.getBlocks().getConversion( state.getBlock().getMaterial() );

			if ( blk == null )
			{
				return null;
			}

			itemstack = new ItemStack( blk, 1 );
			itemstack.setTagInfo( "BlockEntityTag", nbttagcompound );
		}
		else
		{
			switch ( type )
			{
				case MIRROR_DESIGN:
					itemstack = new ItemStack( ChiselsAndBits.getItems().itemMirrorprint );
					break;
				case NEGATIVE_DESIGN:
					itemstack = new ItemStack( ChiselsAndBits.getItems().itemNegativeprint );
					break;
				case POSITIVE_DESIGN:
					itemstack = new ItemStack( ChiselsAndBits.getItems().itemPositiveprint );
					break;
				default:
					return null;
			}

			itemstack.setTagCompound( nbttagcompound );
		}

		if ( side != null )
		{
			itemstack.setTagInfo( "side", new NBTTagByte( (byte) side.ordinal() ) );
		}

		return itemstack;
	}

}
