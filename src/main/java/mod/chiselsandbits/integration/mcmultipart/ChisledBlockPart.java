package mod.chiselsandbits.integration.mcmultipart;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import mcmultipart.multipart.Multipart;
import mcmultipart.raytrace.PartMOP;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;

public class ChisledBlockPart extends Multipart
{
	TileEntityBlockChiseled inner;

	BlockChiseled bc;

	public TileEntityBlockChiseled getTile()
	{
		return inner;
	}

	public BlockChiseled getBlock()
	{
		if ( bc != null )
		{
			return bc;
		}

		return (BlockChiseled) ChiselsAndBits.instance.blocks.getChiseledDefaultState().getBlock();
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
		return getExtendedState( state );
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
			final ByteBuf buf )
	{
		final PacketBuffer pb = new PacketBuffer( buf );
		final NBTTagCompound tag = new NBTTagCompound();
		getTile().writeChisleData( tag );
		pb.writeNBTTagCompoundToBuffer( tag );
	}

	@Override
	public void readUpdatePacket(
			final ByteBuf buf )
	{
		final PacketBuffer pb = new PacketBuffer( buf );
		try
		{
			final NBTTagCompound tag = pb.readNBTTagCompoundFromBuffer();
			getTile().readChisleData( tag );
		}
		catch ( final IOException e )
		{
			// crap update.
		}
	}

}
