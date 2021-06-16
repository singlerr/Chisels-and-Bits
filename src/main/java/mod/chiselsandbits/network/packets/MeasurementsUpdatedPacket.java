package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.measures.MeasuringManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

public final class MeasurementsUpdatedPacket extends ModPacket
{

    public MeasurementsUpdatedPacket(final PacketBuffer buffer)
    {
        super(buffer);
    }

    public MeasurementsUpdatedPacket()
    {
    }

    @Override
    public void writePayload(final PacketBuffer buffer)
    {
        MeasuringManager.getInstance().serializeInto(buffer);
    }

    @Override
    public void readPayload(final PacketBuffer buffer)
    {
        MeasuringManager.getInstance().deserializeFrom(buffer);
    }

    @Override
    public void server(final ServerPlayerEntity playerEntity)
    {
        //Noop
    }

    @Override
    public void client()
    {
        //Noop
    }
}
