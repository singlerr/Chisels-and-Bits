package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.network.handlers.ClientPacketHandlers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class ChangeTrackerUpdatedPacket extends ModPacket
{
    private CompoundNBT tag;

    public ChangeTrackerUpdatedPacket(PacketBuffer byteBuf)
    {
        readPayload(byteBuf);
    }

    public ChangeTrackerUpdatedPacket(final CompoundNBT tag)
    {
        this.tag = tag;
    }

    @Override
    public void writePayload(final PacketBuffer buffer)
    {
        buffer.writeNbt(this.tag);
    }

    @Override
    public void readPayload(final PacketBuffer buffer)
    {
        this.tag = buffer.readAnySizeNbt();
    }

    @Override
    public void client()
    {
        ClientPacketHandlers.handleChangeTrackerUpdated(this.tag);
    }
}
