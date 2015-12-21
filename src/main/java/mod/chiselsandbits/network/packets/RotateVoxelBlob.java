
package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.interfaces.IVoxelBlobItem;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class RotateVoxelBlob extends ModPacket
{

	public int wheel;

	@Override
	public void server(
			final EntityPlayerMP player )
	{
		final ItemStack is = player.getCurrentEquippedItem();
		if ( is != null && is.getItem() instanceof IVoxelBlobItem )
		{
			( (IVoxelBlobItem) is.getItem() ).rotate( is, wheel );
		}
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeInt( wheel );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		wheel = buffer.readInt();
	}

}
