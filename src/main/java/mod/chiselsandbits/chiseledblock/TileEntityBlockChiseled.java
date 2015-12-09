
package mod.chiselsandbits.chiseledblock;

import java.lang.ref.WeakReference;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob.CommonBlock;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobState;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;


public class TileEntityBlockChiseled extends TileEntity
{

	public static final String block_prop = "b";
	public static final String side_prop = "s";
	public static final String opaque_prop = "os";
	public static final String voxel_prop = "v";
	public static final String light_prop = "l";

	private IExtendedBlockState state;

	public TileEntityBlockChiseled()
	{}

	public IExtendedBlockState getState()
	{
		if ( state == null )
		{
			return ( IExtendedBlockState ) ChiselsAndBits.instance.getChiseledDefaultState();
		}
		return state;
	}

	public Block getBlock(
			final Block alternative )
	{
		final IBlockState state = Block.getStateById( getState().getValue( BlockChiseled.block_prop ) );
		if ( state != null )
		{
			return state.getBlock();
		}

		return alternative;
	}

	public IBlockState getParticleBlockState(
			final Block alternative )
	{
		final IBlockState state = Block.getStateById( getState().getValue( BlockChiseled.block_prop ) );
		if ( state != null )
		{
			return state;
		}

		return alternative.getDefaultState();
	}

	public void setState(
			final IExtendedBlockState state )
	{
		this.state = state;
		if ( pos != null && worldObj != null && !isInvalid() )
		{
			// worldObj.checkLight( pos );
		}
	}

	@Override
	public boolean shouldRefresh(
			final World world,
			final BlockPos pos,
			final IBlockState oldState,
			final IBlockState newState)
	{
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public Packet getDescriptionPacket()
	{
		final NBTTagCompound nbttagcompound = new NBTTagCompound();
		writeChisleData( nbttagcompound );

		if ( nbttagcompound.hasNoTags() )
		{
			return null;
		}

		return new S35PacketUpdateTileEntity( pos, 255, nbttagcompound );
	}

	@Override
	public void onDataPacket(
			final NetworkManager net,
			final S35PacketUpdateTileEntity pkt )
	{
		readChisleData( pkt.getNbtCompound() );
		if ( worldObj != null )
		{
			worldObj.markBlockForUpdate( pos );
		}
	}

	public final void writeChisleData(
			final NBTTagCompound compound )
	{
		final Integer b = getState().getValue( BlockChiseled.block_prop );
		final Integer s = getState().getValue( BlockChiseled.side_prop );
		final Integer os = getState().getValue( BlockChiseled.face_opaque );
		final Float l = getState().getValue( BlockChiseled.light_prop );
		final VoxelBlobState vbs = getState().getValue( BlockChiseled.v_prop );

		if ( b == null || vbs == null )
		{
			return;
		}

		if ( b != null && vbs != null )
		{
			compound.setFloat( light_prop, l == null ? 1.0f : l );
			compound.setInteger( block_prop, b );
			compound.setInteger( side_prop, s );
			compound.setInteger( opaque_prop, os);
			compound.setByteArray( voxel_prop, vbs.getByteArray() );
		}
	}

	public final void readChisleData(
			final NBTTagCompound compound )
	{
		final int opaqueFlags = compound.getInteger( opaque_prop );
		final int sideFlags = compound.getInteger( side_prop );
		int b = compound.getInteger( block_prop );
		final float l = compound.getFloat( light_prop );
		final byte[] v = compound.getByteArray( voxel_prop );

		if ( b == 0 )
		{
			b = Block.getStateId( Blocks.cobblestone.getDefaultState() ); // if load fails default to cobble stone...
		}

		setState( getState().withProperty( BlockChiseled.side_prop, sideFlags ).withProperty( BlockChiseled.face_opaque,  opaqueFlags ).withProperty( BlockChiseled.block_prop, b ).withProperty( BlockChiseled.light_prop, l ).withProperty( BlockChiseled.v_prop, new VoxelBlobState( v, getPositionRandom( pos ) ) ) );
	}

	@Override
	public void writeToNBT(
			final NBTTagCompound compound )
	{
		super.writeToNBT( compound );
		writeChisleData( compound );
	}

	@Override
	public void readFromNBT(
			final NBTTagCompound compound )
	{
		super.readFromNBT( compound );
		readChisleData( compound );
	}

	public void fillWith(
			final IBlockState blockType )
	{
		final VoxelBlob vb = new VoxelBlob();
		vb.fill( Block.getStateId( blockType ) );

		int ref = 0;
		final CommonBlock cb = vb.mostCommonBlock();
		if ( cb != null )
		{
			ref = cb.ref;
		}

		IExtendedBlockState state = getState().withProperty( BlockChiseled.side_prop, 0xFF ).withProperty( BlockChiseled.light_prop, vb.getOpacity() ).withProperty( BlockChiseled.v_prop, new VoxelBlobState( vb, getPositionRandom( pos ) ) );

		// required for placing bits
		if ( ref != 0 )
		{
			state = state.withProperty( BlockChiseled.block_prop, ref );
		}

		setState( state );
		markDirty();
	}

	private long getPositionRandom(
			final BlockPos pos )
	{
		if ( FMLCommonHandler.instance().getSide() == Side.CLIENT )
		{
			return MathHelper.getPositionRandom( pos );
		}

		return 0;
	}

	WeakReference<VoxelBlob> blob;

	public VoxelBlob getBlob()
	{
		VoxelBlob vb = blob == null ? null : blob.get();

		if ( vb == null )
		{
			final VoxelBlobState vbs = getState().getValue( BlockChiseled.v_prop );
			
			if ( vbs != null )
			{
				vb = vbs.getVoxelBlob();
				
				if ( vb == null )
				{
					vb = new VoxelBlob();
					vb.fill( Block.getStateId( Blocks.cobblestone.getDefaultState() ) );
				}
				
				blob = new WeakReference<VoxelBlob>( vb );
			}
			else
				vb = new VoxelBlob();
		}

		return vb;
	}

	public void setBlob(
			final VoxelBlob vb )
	{
		blob = new WeakReference<VoxelBlob>( vb );

		final CommonBlock common = vb.mostCommonBlock();
		final float opacity = vb.getOpacity();

		final int sideFlags = vb.getSideFlags(5,11);
		
		final VoxelBlob vbX = new VoxelBlob(vb);
		vbX.filter(EnumWorldBlockLayer.SOLID);
		
		final int opaqueFlags = vbX.getSideFlags(5,11);

		final Integer oldOpaqueFlags = getState().getValue( BlockChiseled.face_opaque );
		final Integer oldSideFlags = getState().getValue( BlockChiseled.side_prop );

		if ( worldObj == null )
		{
			setState( getState().withProperty( BlockChiseled.side_prop, sideFlags ).withProperty( BlockChiseled.face_opaque, opaqueFlags ).withProperty( BlockChiseled.v_prop, new VoxelBlobState( vb.toByteArray(), getPositionRandom( pos ) ) ).withProperty( BlockChiseled.light_prop, opacity ).withProperty( BlockChiseled.block_prop, common.ref ) );
			return;
		}

		if ( common.isFull )
		{
			worldObj.setBlockState( pos, Block.getStateById( common.ref ) );
		}
		else if ( common.ref != 0 )
		{
			setState( getState().withProperty( BlockChiseled.side_prop, sideFlags ).withProperty( BlockChiseled.face_opaque, opaqueFlags ).withProperty( BlockChiseled.v_prop, new VoxelBlobState( vb.toByteArray(), getPositionRandom( pos ) ) ).withProperty( BlockChiseled.light_prop, opacity ).withProperty( BlockChiseled.block_prop, common.ref ) );
			markDirty();
			worldObj.markBlockForUpdate( pos );

			if ( oldSideFlags == null || oldOpaqueFlags == null || oldSideFlags != sideFlags || oldOpaqueFlags != opaqueFlags )
			{
				worldObj.notifyNeighborsOfStateChange( pos, worldObj.getBlockState( pos ).getBlock() );
			}
		}
		else
		{
			worldObj.setBlockToAir( pos ); // no physical matter left...
		}
	}

	public ItemStack getItemStack(
			final Block what,
			final EntityPlayer player )
	{
		final ItemStack itemstack = new ItemStack( what, 1 );

		if ( player != null )
		{
			EnumFacing enumfacing = ModUtil.getPlaceFace( player );

			final TileEntityBlockChiseled test = new TileEntityBlockChiseled();
			VoxelBlob vb = getBlob();

			int rotations = ModUtil.getRotationIndex( enumfacing );
			while ( rotations > 0 )
			{
				rotations--;
				enumfacing = enumfacing.rotateYCCW();
				vb = vb.spin( Axis.Y );
			}

			test.setBlob( vb );

			final NBTTagCompound nbttagcompound = new NBTTagCompound();
			test.writeChisleData( nbttagcompound );
			itemstack.setTagInfo( "BlockEntityTag", nbttagcompound );

			itemstack.setTagInfo( "side", new NBTTagByte( ( byte ) enumfacing.ordinal() ) );
		}
		else
		{
			final NBTTagCompound nbttagcompound = new NBTTagCompound();
			writeChisleData( nbttagcompound );
			itemstack.setTagInfo( "BlockEntityTag", nbttagcompound );
		}
		return itemstack;
	}

	public boolean isSideSolid(
			final EnumFacing side )
	{
		final Integer sideFlags = getState().getValue( BlockChiseled.side_prop );

		if ( sideFlags == null )
		{
			return true; // if torches or other blocks are on the block this prevents a conversion from crashing.
		}

		return ( sideFlags & 1 << side.ordinal() ) != 0;
	}

	public boolean isSideOpaque(EnumFacing side) {
		final Integer sideFlags = getState().getValue( BlockChiseled.face_opaque );

		if ( sideFlags == null )
		{
			return false; // if torches or other blocks are on the block this prevents a conversion from crashing.
		}

		return ( sideFlags & 1 << side.ordinal() ) != 0;
	}
}
