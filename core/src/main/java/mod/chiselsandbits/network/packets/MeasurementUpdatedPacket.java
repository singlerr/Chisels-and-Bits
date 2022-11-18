package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.measures.Measurement;
import mod.chiselsandbits.measures.MeasuringManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;

public final class MeasurementUpdatedPacket extends ModPacket
{

    private Measurement measurement;

    public MeasurementUpdatedPacket(final Measurement measurement)
    {
        this.measurement = measurement;
    }

    public MeasurementUpdatedPacket(final FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        measurement.serializeInto(buffer);
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
        this.measurement = new Measurement();
        this.measurement.deserializeFrom(buffer);
    }

    @Override
    public void server(final ServerPlayer playerEntity)
    {
        MeasuringManager.getInstance().addOrUpdate(measurement);
    }
}
