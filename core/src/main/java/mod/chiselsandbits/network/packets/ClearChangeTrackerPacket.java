package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class ClearChangeTrackerPacket extends ModPacket
{
    public ClearChangeTrackerPacket(FriendlyByteBuf byteBuf)
    {
        this.readPayload(byteBuf);
    }

    public ClearChangeTrackerPacket()
    {
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
        final IChangeTracker tracker = IChangeTrackerManager.getInstance().getChangeTracker(playerEntity);
        tracker.clear();
    }
}
