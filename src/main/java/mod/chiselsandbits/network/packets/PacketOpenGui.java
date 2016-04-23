package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.client.gui.ModGuiTypes;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

public class PacketOpenGui extends ModPacket
{
	String name;

	public PacketOpenGui()
	{
		// required...
	}

	public PacketOpenGui(
			final ModGuiTypes guiType )
	{
		name = guiType.name();
	}

	@Override
	public void server(
			final EntityPlayerMP player )
	{
		try
		{
			player.openGui( ChiselsAndBits.getInstance(), Enum.valueOf( ModGuiTypes.class, name ).ordinal(), player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ );
		}
		catch ( final IllegalArgumentException iae )
		{
			Log.logError( "Bag Gui Packet.", iae );
		}
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeString( name );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		name = buffer.readStringFromBuffer( 32 );
	}

}
