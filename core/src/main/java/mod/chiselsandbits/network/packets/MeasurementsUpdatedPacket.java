package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.measures.MeasuringManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;

public final class MeasurementsUpdatedPacket extends ModPacket
{

    public MeasurementsUpdatedPacket(final FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    public MeasurementsUpdatedPacket()
    {
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        MeasuringManager.getInstance().serializeInto(buffer);
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
        MeasuringManager.getInstance().deserializeFrom(buffer);
    }

    @Override
    public void server(final ServerPlayer playerEntity)
    {
        //Noop
    }

    @Override
    public void client()
    {
        //Noop
    }
}
