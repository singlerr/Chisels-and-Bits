package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.measures.Measurement;
import mod.chiselsandbits.measures.MeasuringManager;
import net.minecraft.network.PacketBuffer;

public final class MeasurementUpdatedPacket extends ModPacket
{

    private final Measurement measurement;

    public MeasurementUpdatedPacket(final Measurement measurement)
    {
        this.measurement = measurement;
    }

    public MeasurementUpdatedPacket(PacketBuffer buffer)
    {
        this.measurement = new Measurement();
        readPayload(buffer);
    }

    @Override
    public void writePayload(final PacketBuffer buffer)
    {
        measurement.serializeInto(buffer);
    }

    @Override
    public void readPayload(final PacketBuffer buffer)
    {
        measurement.deserializeFrom(buffer);
        MeasuringManager.getInstance().addOrUpdate(this.measurement);
    }
}
