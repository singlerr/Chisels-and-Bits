package mod.chiselsandbits.network.packets;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.ChiselMode;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.ChiselTypeIterator;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ChiselInventory;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class PacketChisel extends ModPacket
{

	BlockPos pos;

	int x, y, z;
	int from_x, from_y, from_z;

	EnumFacing side;
	ChiselMode mode;

	public PacketChisel()
	{
	}

	public PacketChisel(
			final BlockPos pos,
			final int x,
			final int y,
			final int z,
			final int fx,
			final int fy,
			final int fz,
			final EnumFacing side,
			final ChiselMode mode )
	{
		this.pos = pos;
		this.x = x;
		this.y = y;
		this.z = z;
		from_x = fx;
		from_y = fy;
		from_z = fz;
		this.side = side;
		this.mode = mode;
	}

	public PacketChisel(
			final BlockPos pos,
			final int x,
			final int y,
			final int z,
			final EnumFacing side,
			final ChiselMode mode )
	{
		this.pos = pos;
		this.x = x;
		this.y = y;
		this.z = z;
		this.side = side;
		this.mode = mode;
	}

	@Override
	public void server(
			final EntityPlayerMP playerEntity )
	{
		doAction( playerEntity );
	}

	public int doAction(
			final EntityPlayer player )
	{
		final World world = player.worldObj;
		final ChiselInventory chisel = new ChiselInventory( player, pos, side );

		IBlockState blkstate = world.getBlockState( pos );
		Block blkObj = blkstate.getBlock();

		if ( !chisel.isValid() || blkObj == null || blkstate == null || !ItemChisel.canMine( chisel, blkstate, player, world, pos ) )
		{
			return 0;
		}

		if ( BlockChiseled.replaceWithChisled( world, pos, blkstate ) )
		{
			blkstate = world.getBlockState( pos );
			blkObj = blkstate.getBlock();
		}

		final TileEntity te = ModUtil.getChiseledTileEntity( world, pos, false );
		if ( te instanceof TileEntityBlockChiseled && chisel.isValid() )
		{
			final TileEntityBlockChiseled tec = (TileEntityBlockChiseled) te;

			// adjust voxel state...
			final VoxelBlob vb = tec.getBlob();

			ItemStack extracted = null;

			final List<EntityItem> spawnlist = new ArrayList<EntityItem>();

			final ChiselTypeIterator i = getIterator( vb );
			while ( i.hasNext() && chisel.isValid() )
			{
				extracted = ItemChisel.chiselBlock( chisel, player, vb, world, pos, i.side, i.x(), i.y(), i.z(), extracted, spawnlist );
			}

			for ( final EntityItem ei : spawnlist )
			{
				world.spawnEntityInWorld( ei );
			}

			if ( extracted != null )
			{
				tec.postChisel( vb );
				return ItemChisel.getStackState( extracted );
			}
		}

		return 0;
	}

	private ChiselTypeIterator getIterator(
			final VoxelBlob vb )
	{
		if ( mode == ChiselMode.DRAWN_REGION )
		{
			final int lowX = Math.max( 0, Math.min( x, from_x ) );
			final int lowY = Math.max( 0, Math.min( y, from_y ) );
			final int lowZ = Math.max( 0, Math.min( z, from_z ) );

			final int highX = Math.min( VoxelBlob.dim, Math.max( x, from_x ) );
			final int highY = Math.min( VoxelBlob.dim, Math.max( y, from_y ) );
			final int highZ = Math.min( VoxelBlob.dim, Math.max( z, from_z ) );

			return new ChiselTypeIterator( VoxelBlob.dim, lowX, lowY, lowZ, 1 + highX - lowX, 1 + highY - lowY, 1 + highZ - lowZ, side );
		}

		return new ChiselTypeIterator( VoxelBlob.dim, x, y, z, vb, mode, side );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		pos = buffer.readBlockPos();

		int value = buffer.readInt();

		x = value & 0xF;
		value = value >>> 4;

		y = value & 0xF;
		value = value >>> 4;

		z = value & 0xF;
		value = value >>> 4;

		int value2 = buffer.readInt();

		from_x = value2 & 0xF;
		value2 = value2 >>> 4;

		from_y = value2 & 0xF;
		value2 = value2 >>> 4;

		from_z = value2 & 0xF;
		value2 = value2 >>> 4;

		side = EnumFacing.values()[value & 0x7];
		value = value >>> 3;

		mode = ChiselMode.values()[value];
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeBlockPos( pos );

		int value = mode.ordinal();
		value = value << 3;

		value = value | side.ordinal();
		value = value << 4;

		value = value | z;
		value = value << 4;

		value = value | y;
		value = value << 4;

		value = value | x;
		buffer.writeInt( value );

		int value2 = 0;
		value2 = value2 | from_z;
		value2 = value2 << 4;

		value2 = value2 | from_y;
		value2 = value2 << 4;

		value2 = value2 | from_x;
		buffer.writeInt( value2 );
	}

}
