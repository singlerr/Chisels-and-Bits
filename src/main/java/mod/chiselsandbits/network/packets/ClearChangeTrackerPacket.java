package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import mod.chiselsandbits.api.util.LocalStrings;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;

public class ClearChangeTrackerPacket extends ModPacket
{
    public ClearChangeTrackerPacket(PacketBuffer byteBuf)
    {
        this.readPayload(byteBuf);
    }

    public ClearChangeTrackerPacket()
    {
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
        final IChangeTracker tracker = IChangeTrackerManager.getInstance().getChangeTracker(playerEntity);
        tracker.clear();
    }
}
