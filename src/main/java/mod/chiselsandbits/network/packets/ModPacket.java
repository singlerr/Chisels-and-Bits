package mod.chiselsandbits.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public abstract class ModPacket
{

    public ModPacket()
    {
    }

    public ModPacket(FriendlyByteBuf buffer) {
        readPayload(buffer);
    }

    public void server(
			final ServerPlayer playerEntity )
	{
		throw new RuntimeException( getClass().getName() + " is not a server packet." );
	}

	public void client()
	{
		throw new RuntimeException( getClass().getName() + " is not a client packet." );
	}

	abstract public void writePayload(
			FriendlyByteBuf buffer );

	abstract public void readPayload(
			FriendlyByteBuf buffer );

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
