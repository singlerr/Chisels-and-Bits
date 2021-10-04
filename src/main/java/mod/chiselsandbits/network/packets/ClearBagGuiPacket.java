package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.container.BagContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;

public final class ClearBagGuiPacket extends ModPacket
{
    private ItemStack stack = null;

    public ClearBagGuiPacket(final FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    public ClearBagGuiPacket(
      final ItemStack inHandItem)
    {
        stack = inHandItem;
    }

    @Override
    public void server(
      final ServerPlayer player)
    {
        execute(player);
    }

    public void execute(final Player player) {
        if (player.containerMenu instanceof BagContainer)
        {
            ((BagContainer) player.containerMenu).clear(stack);
        }
    }

    @Override
    public void readPayload(
      final FriendlyByteBuf buffer)
    {
        stack = buffer.readItem();
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        buffer.writeItem(stack);
    }

}
