package mod.chiselsandbits.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public abstract class ModPacket
{

    public ModPacket()
    {
    }

    public void server(
			final ServerPlayerEntity playerEntity )
	{
		throw new RuntimeException( getClass().getName() + " is not a server packet." );
	}

	public void client()
	{
		throw new RuntimeException( getClass().getName() + " is not a client packet." );
	}

	abstract public void writePayload(
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
			server( context.getSender() );
		}
	}

}
