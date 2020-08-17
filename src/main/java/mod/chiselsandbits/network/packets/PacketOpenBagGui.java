package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.client.gui.ModGuiTypes;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

public class PacketOpenBagGui extends ModPacket
{
    public PacketOpenBagGui(PacketBuffer buffer)
    {
        readPayload(buffer);
    }

    public PacketOpenBagGui()
    {
    }

    @Override
	public void server(
			final ServerPlayerEntity player )
	{
		player.openContainer( ChiselsAndBits.getInstance(), ModGuiTypes.BitBag.ordinal(), player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ );
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		// no data...
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		// no data..
	}

}
