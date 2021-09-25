package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.container.BagContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public final class ClearBagGuiPacket extends ModPacket
{
    private ItemStack stack = null;

    public ClearBagGuiPacket(final PacketBuffer buffer)
    {
        super(buffer);
    }

    public ClearBagGuiPacket(
      final ItemStack inHandItem)
    {
        stack = inHandItem;
    }

    @Override
    public void server(
      final ServerPlayerEntity player)
    {
        if (player.containerMenu instanceof BagContainer)
        {
            ((BagContainer) player.containerMenu).clear(stack);
        }
    }

    @Override
    public void readPayload(
      final PacketBuffer buffer)
    {
        stack = buffer.readItem();
    }

    @Override
    public void writePayload(final PacketBuffer buffer)
    {
        buffer.writeItem(stack);
    }

}
