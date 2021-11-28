package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.network.handlers.ClientPacketHandlers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public final class ChangeTrackerUpdatedPacket extends ModPacket
{
    private CompoundTag tag;

    public ChangeTrackerUpdatedPacket(FriendlyByteBuf byteBuf)
    {
        readPayload(byteBuf);
    }

    public ChangeTrackerUpdatedPacket(final CompoundTag tag)
    {
        this.tag = tag;
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        buffer.writeNbt(this.tag);
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
        this.tag = buffer.readAnySizeNbt();
    }

    @Override
    public void client()
    {
        ClientPacketHandlers.handleChangeTrackerUpdated(this.tag);
    }
}
