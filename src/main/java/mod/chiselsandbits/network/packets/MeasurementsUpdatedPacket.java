package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.measures.MeasuringManager;
import net.minecraft.network.PacketBuffer;

public final class MeasurementsUpdatedPacket extends ModPacket
{

    public MeasurementsUpdatedPacket()
    {
    }

    public MeasurementsUpdatedPacket(PacketBuffer buffer)
    {
        readPayload(buffer);
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
}
