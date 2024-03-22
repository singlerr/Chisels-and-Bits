package mod.chiselsandbits.network.packets;

import com.communi.suggestu.scena.core.entity.IPlayerInventoryManager;
import mod.chiselsandbits.item.multistate.SingleBlockMultiStateItemStack;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GivePlayerPatternCommandPacket extends ModPacket
{

    private CompoundTag tag;

    public GivePlayerPatternCommandPacket(CompoundTag tag) {
        this.tag = tag;
    }

    public GivePlayerPatternCommandPacket(final FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        buffer.writeNbt(tag);
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
        tag = buffer.readNbt();
    }

    @Override
    public void server(final ServerPlayer playerEntity)
    {
        final Item item = ModItems.SINGLE_USE_PATTERN_ITEM.get();
        final SingleBlockMultiStateItemStack patternStack = new SingleBlockMultiStateItemStack(item, tag);

        IPlayerInventoryManager.getInstance().giveToPlayer(playerEntity, patternStack.toPatternStack());
    }
}
