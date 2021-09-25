package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.container.BagContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

public final class SortBagGuiPacket extends ModPacket
{
    public SortBagGuiPacket(PacketBuffer buffer)
    {
        super(buffer);
    }

    public SortBagGuiPacket()
    {
    }

    @Override
    public void server(
      final ServerPlayerEntity player)
    {
        if (player.containerMenu instanceof BagContainer)
        {
            ((BagContainer) player.containerMenu).sort();
        }
    }

    @Override
    public void writePayload(final PacketBuffer buffer)
    {
    }

    @Override
    public void readPayload(
      PacketBuffer buffer)
    {
    }
}
