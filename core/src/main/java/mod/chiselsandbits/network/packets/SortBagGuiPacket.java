package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.container.BagContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public final class SortBagGuiPacket extends ModPacket
{
    public SortBagGuiPacket(FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    public SortBagGuiPacket()
    {
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
            ((BagContainer) player.containerMenu).sort();
        }
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
    }

    @Override
    public void readPayload(
      FriendlyByteBuf buffer)
    {
    }
}
