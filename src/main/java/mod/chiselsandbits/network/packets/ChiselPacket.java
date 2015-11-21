
package mod.chiselsandbits.network.packets;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.ChiselMode;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.ChiselTypeIterator;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;
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


public class ChiselPacket extends ModPacket
{

	BlockPos pos;

	int x, y, z;
	EnumFacing side;
	ChiselMode mode;

	public ChiselPacket()
	{}

	public ChiselPacket(
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
		ItemStackSlot chisel = ModUtil.findChisel( player );

		IBlockState blkstate = world.getBlockState( pos );
		Block blkObj = blkstate.getBlock();
		
		if ( blkObj == null || blkstate == null || ! ItemChisel.canMine( chisel.getStack(), blkstate, player, world, pos ) )
			return 0;
		
		if ( BlockChiseled.replaceWithChisled( world, pos, blkstate ) )
		{
			blkstate = world.getBlockState( pos );
			blkObj = blkstate.getBlock();
		}

		if ( blkObj instanceof BlockChiseled )
		{
			final TileEntity te = world.getTileEntity( pos );
			if ( te instanceof TileEntityBlockChiseled && chisel.isValid() )
			{
				if ( !player.canPlayerEdit( pos, side, chisel.getStack() ) )
					return 0;

				final TileEntityBlockChiseled tec = ( TileEntityBlockChiseled ) te;

				// adjust voxel state...
				final VoxelBlob vb = tec.getBlob();

				ItemStack extracted = null;

				final List<EntityItem> spawnlist = new ArrayList<EntityItem>();

				final ChiselTypeIterator i = new ChiselTypeIterator( VoxelBlob.dim, x, y, z, vb, mode, side );
				while ( i.hasNext() )
				{
					extracted = ItemChisel.chiselBlock( player, vb, world, pos, i.side, i.x(), i.y(), i.z(), extracted, spawnlist );

					if ( extracted != null )
					{
						chisel.damage( player );
						if ( !chisel.isValid() )
						{
							chisel = ModUtil.findChisel( player );
							if ( !chisel.isValid() )
							{
								break;
							}
						}
					}
				}

				for ( final EntityItem ei : spawnlist )
				{
					world.spawnEntityInWorld( ei );
				}

				if ( extracted != null )
				{
					tec.setBlob( vb );
					return ItemChisel.getStackState( extracted );
				}
			}
		}

		return 0;
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
	}

}
