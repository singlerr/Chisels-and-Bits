package mod.chiselsandbits.network.packets;

import com.communi.suggestu.scena.core.dist.DistExecutor;
import mod.chiselsandbits.network.handlers.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import static com.communi.suggestu.scena.core.dist.Dist.CLIENT;

public final class AddMultiStateItemStackToClipboardPacket extends ModPacket
{

    private ItemStack stack = ItemStack.EMPTY;

    public AddMultiStateItemStackToClipboardPacket(final ItemStack stack) {
        this.stack = stack;
    }

    public AddMultiStateItemStackToClipboardPacket(final FriendlyByteBuf byteBuf)
    {
        readPayload(byteBuf);
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        buffer.writeItem(stack);
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
        stack = buffer.readItem();
    }

    @Override
    public void client()
    {
        DistExecutor.unsafeRunWhenOn(CLIENT, () -> () -> ClientPacketHandlers.handleAddMultiStateToClipboard(stack));
    }
}
