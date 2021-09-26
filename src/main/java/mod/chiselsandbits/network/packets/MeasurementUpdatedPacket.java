package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.measures.Measurement;
import mod.chiselsandbits.measures.MeasuringManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

public final class MeasurementUpdatedPacket extends ModPacket
{

    private Measurement measurement;

    public MeasurementUpdatedPacket(final Measurement measurement)
    {
        this.measurement = measurement;
    }

    public MeasurementUpdatedPacket(final PacketBuffer buffer)
    {
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
        this.measurement = new Measurement();
        this.measurement.deserializeFrom(buffer);
    }

    @Override
    public void server(final ServerPlayerEntity playerEntity)
    {
        MeasuringManager.getInstance().addOrUpdate(measurement);
    }
}
