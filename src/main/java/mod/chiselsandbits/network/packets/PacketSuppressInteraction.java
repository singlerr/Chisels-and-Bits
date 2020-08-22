package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.events.EventPlayerInteract;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

public class PacketSuppressInteraction extends ModPacket
{

	private boolean newSetting = false;

	public PacketSuppressInteraction(final PacketBuffer buffer) {
	    readPayload(buffer);
    }

    public PacketSuppressInteraction(final boolean newSetting)
    {
        this.newSetting = newSetting;
    }

    @Override
	public void server(
			final ServerPlayerEntity player )
	{
		EventPlayerInteract.setPlayerSuppressionState( player, newSetting );
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeBoolean( newSetting );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		newSetting = buffer.readBoolean();
	}

}
