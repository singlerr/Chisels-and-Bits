package mod.chiselsandbits.network.packets;

import java.io.IOException;

import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class PacketAccurateSneakPlace extends ModPacket
{

	boolean good = true;

	public ItemStack stack;
	public BlockPos pos;
	public EnumHand hand;
	public EnumFacing side;
	public float hitX, hitY, hitZ;

	@Override
	public void server(
			final EntityPlayerMP playerEntity )
	{
		if ( good && stack != null && stack.getItem() instanceof ItemBlockChiseled )
		{
			ItemStack inHand = playerEntity.getHeldItem( hand );
			if ( ItemStack.areItemStackTagsEqual( stack, inHand ) )
			{
				if ( playerEntity.capabilities.isCreativeMode )
				{
					inHand = stack;
				}

				final ItemBlockChiseled ibc = (ItemBlockChiseled) stack.getItem();
				ibc.doItemUse( inHand, playerEntity, playerEntity.worldObj, pos, hand, side, hitX, hitY, hitZ );

				if ( !playerEntity.capabilities.isCreativeMode && inHand.stackSize <= 0 )
				{
					playerEntity.setHeldItem( hand, null );
				}
			}
		}
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeItemStackToBuffer( stack );
		buffer.writeBlockPos( pos );
		buffer.writeEnumValue( side );
		buffer.writeEnumValue( hand );
		buffer.writeFloat( hitX );
		buffer.writeFloat( hitY );
		buffer.writeFloat( hitZ );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		try
		{
			stack = buffer.readItemStackFromBuffer();
			pos = buffer.readBlockPos();
			side = buffer.readEnumValue( EnumFacing.class );
			hand = buffer.readEnumValue( EnumHand.class );
			hitX = buffer.readFloat();
			hitY = buffer.readFloat();
			hitZ = buffer.readFloat();
		}
		catch ( final IOException e )
		{
			good = false;
		}
	}

}
