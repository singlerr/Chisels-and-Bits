package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.measures.MeasuringManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;

public final class MeasurementsResetPacket extends ModPacket
{
    public MeasurementsResetPacket()
    {
    }

    public MeasurementsResetPacket(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
    }

    @Override
    public void server(final ServerPlayer playerEntity)
    {
        MeasuringManager.getInstance().resetMeasurementsFor(playerEntity);
    }
}
