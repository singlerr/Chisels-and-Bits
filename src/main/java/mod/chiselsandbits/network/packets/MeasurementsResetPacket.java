package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.measures.MeasuringManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

public final class MeasurementsResetPacket extends ModPacket
{
    public MeasurementsResetPacket()
    {
    }

    public MeasurementsResetPacket(final PacketBuffer buffer)
    {
        readPayload(buffer);
    }

    @Override
    public void writePayload(final PacketBuffer buffer)
    {
    }

    @Override
    public void readPayload(final PacketBuffer buffer)
    {
    }

    @Override
    public void server(final ServerPlayerEntity playerEntity)
    {
        MeasuringManager.getInstance().resetMeasurementsFor(playerEntity);
    }
}
