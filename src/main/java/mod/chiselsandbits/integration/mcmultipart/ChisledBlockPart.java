package mod.chiselsandbits.integration.mcmultipart;

import java.io.IOException;
import java.util.List;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IOccludingPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils.RayTraceResultPart;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.BitOcclusionIterator;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class ChisledBlockPart extends Multipart implements IOccludingPart
{
	TileEntityBlockChiseled inner;
	BlockChiseled bc;

	@Override
	public boolean occlusionTest(
			final IMultipart part )
	{
		return super.occlusionTest( part ) && !( part instanceof ChisledBlockPart );
	}

	@Override
	public void addCollisionBoxes(
			final AxisAlignedBB mask,
			final List<AxisAlignedBB> list,
			final Entity collidingEntity )
	{
		getBlock().addCollisionBoxesToList( getTile(), BlockPos.ORIGIN, mask, list, collidingEntity );
	}

	@Override
	public RayTraceResultPart collisionRayTrace(
			final Vec3 start,
			final Vec3 end )
	{
		final MovingObjectPosition mop = getBlock().collisionRayTrace( getTile(), getPos(), start, end, true );

		if ( mop == null )
		{
			return null;
		}

		final AxisAlignedBB bb = getBlock().getSelectedBoundingBox( getTile(), getPos() );
		return new RayTraceResultPart( new PartMOP( mop, this ), bb == null ? null : bb.offset( -getPos().getX(), -getPos().getY(), -getPos().getZ() ) );
	}

	@Override
	public void addOcclusionBoxes(
			final List<AxisAlignedBB> list )
	{
		final VoxelBlob blob = getTile().getBlob();
		final BitOcclusionIterator boi = new BitOcclusionIterator( list );

		while ( boi.hasNext() )
		{
			if ( boi.getNext( blob ) != 0 )
			{
				boi.add();
			}
			else
			{
				boi.drop();
			}
		}
	}

	public TileEntityBlockChiseled getTile()
	{
		if ( inner == null )
		{
			inner = new TileEntityBlockChiseled();
		}

		// update tile stats..
		inner.setWorldObj( getWorld() );
		inner.setPos( getPos() );

		return inner;
	}

	public BlockChiseled getBlock()
	{
		if ( bc == null )
		{
			bc = (BlockChiseled) ChiselsAndBits.instance.blocks.getChiseledDefaultState().getBlock();
		}

		return bc;
	}

	@Override
	public String getModelPath()
	{
		return getBlock().getModel();
	}

	@Override
	public int getLightValue()
	{
		return getTile().getBasicState().getValue( BlockChiseled.light_prop );
	}

	@Override
	public ItemStack getPickBlock(
			final EntityPlayer player,
			final PartMOP hit )
	{
		return getBlock().getPickBlock( hit, hit.getBlockPos(), getTile() );
	}

	@Override
	public boolean canRenderInLayer(
			final EnumWorldBlockLayer layer )
	{
		return true;
	}

	@Override
	public IBlockState getExtendedState(
			final IBlockState state )
	{
		return getTile().getRenderState();
	}

	@Override
	public BlockState createBlockState()
	{
		return getBlock().getBlockState();
	}

	@Override
	public boolean rotatePart(
			final EnumFacing axis )
	{
		getTile().rotateBlock( axis );
		return true;
	}

	@Override
	public EnumFacing[] getValidRotations()
	{
		return EnumFacing.VALUES;
	}

	@Override
	public void writeToNBT(
			final NBTTagCompound tag )
	{
		getTile().writeChisleData( tag );
	}

	@Override
	public void readFromNBT(
			final NBTTagCompound tag )
	{
		getTile().readChisleData( tag );
	}

	@Override
	public void writeUpdatePacket(
			final PacketBuffer buf )
	{
		final NBTTagCompound tag = new NBTTagCompound();
		getTile().writeChisleData( tag );
		buf.writeNBTTagCompoundToBuffer( tag );
	}

	@Override
	public void readUpdatePacket(
			final PacketBuffer buf )
	{
		try
		{
			final NBTTagCompound tag = buf.readNBTTagCompoundFromBuffer();
			getTile().readChisleData( tag );
		}
		catch ( final IOException e )
		{
			// crap update.
		}
	}
}
