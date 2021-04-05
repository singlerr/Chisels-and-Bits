package mod.chiselsandbits.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public abstract class ModPacket
{

	ServerPlayerEntity serverEntity = null;

	public void server(
			final ServerPlayerEntity playerEntity )
	{
		throw new RuntimeException( getClass().getName() + " is not a server packet." );
	}

	public void client()
	{
		throw new RuntimeException( getClass().getName() + " is not a client packet." );
	}

	abstract public void getPayload(
			PacketBuffer buffer );

	abstract public void readPayload(
			PacketBuffer buffer );

	public void processPacket(
			final NetworkEvent.Context context,
            final Boolean onServer)
	{
		if (!onServer)
		{
			client();
		}
		else
		{
		    serverEntity = context.getSender();
			server( serverEntity );
		}
	}

}
