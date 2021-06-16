package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.container.BagContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

public final class BagGuiStackPacket extends ModPacket
{
    private int       index = -1;
    private ItemStack stack;

    public BagGuiStackPacket(final PacketBuffer buffer)
    {
        super(buffer);
    }

    public BagGuiStackPacket(final int index, @NotNull final ItemStack stack)
    {
        this.index = index;
        this.stack = stack;
    }

    @Override
    public void client()
    {
        if (Minecraft.getInstance().player == null)
        {
            return;
        }

        if (Minecraft.getInstance().player.openContainer == null)
        {
            return;
        }

        final Container cc = Minecraft.getInstance().player.openContainer;
        if (cc instanceof BagContainer)
        {
            ((BagContainer) cc).customSlots.get(index).putStack(stack);
        }
    }

    @Override
    public void readPayload(
      final PacketBuffer buffer)
    {
        index = buffer.readInt();
        stack = buffer.readItemStack();
        stack.setCount(buffer.readVarInt());
    }

    @Override
    public void writePayload(final PacketBuffer buffer)
    {
        buffer.writeInt(index);

        final ItemStack networkStack = stack.copy();
        networkStack.setCount(1);
        buffer.writeItemStack(networkStack);

        buffer.writeVarInt(stack.getCount());
    }
}
