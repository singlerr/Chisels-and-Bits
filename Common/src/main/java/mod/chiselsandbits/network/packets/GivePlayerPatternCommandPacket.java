package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.platforms.core.entity.IPlayerInventoryManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class GivePlayerPatternCommandPacket extends ModPacket
{

    private ItemStack patternStack;

    public GivePlayerPatternCommandPacket(final ItemStack patternStack) {this.patternStack = patternStack;}

    public GivePlayerPatternCommandPacket(final FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        buffer.writeItem(patternStack);
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
        patternStack = buffer.readItem();
    }

    @Override
    public void server(final ServerPlayer playerEntity)
    {
        IPlayerInventoryManager.getInstance().giveToPlayer(playerEntity, patternStack);
    }
}
