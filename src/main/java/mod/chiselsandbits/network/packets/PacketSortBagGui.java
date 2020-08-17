package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.bitbag.BagContainer;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

public class PacketSortBagGui extends ModPacket
{
    public PacketSortBagGui(PacketBuffer buffer) {
        readPayload(buffer);
    }

	public PacketSortBagGui()
	{
	}

	@Override
	public void server(
			final ServerPlayerEntity player )
	{
		if ( player.openContainer instanceof BagContainer )
		{
			( (BagContainer) player.openContainer ).sort();
		}
	}

	@Override
	public void getPayload(
			PacketBuffer buffer )
	{
	}

	@Override
	public void readPayload(
			PacketBuffer buffer )
	{
	}

}
