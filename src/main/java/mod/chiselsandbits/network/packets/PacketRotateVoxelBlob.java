package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.interfaces.IVoxelBlobItem;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;

public class PacketRotateVoxelBlob extends ModPacket
{

	public Axis axis;
	public Rotation rotation;

	@Override
	public void server(
			final EntityPlayerMP player )
	{
		final ItemStack is = player.getHeldItemMainhand();
		if ( is != null && is.getItem() instanceof IVoxelBlobItem )
		{
			( (IVoxelBlobItem) is.getItem() ).rotate( is, axis, rotation );
		}
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeEnumValue( axis );
		buffer.writeEnumValue( rotation );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		axis = buffer.readEnumValue( Axis.class );
		rotation = buffer.readEnumValue( Rotation.class );
	}

}
