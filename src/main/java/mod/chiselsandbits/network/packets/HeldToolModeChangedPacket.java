package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public final class HeldToolModeChangedPacket extends ModPacket
{

    private int modeIndex;

    public HeldToolModeChangedPacket(final PacketBuffer buffer)
    {
        super(buffer);
    }

    public HeldToolModeChangedPacket(final int modeIndex)
    {
        this.modeIndex = modeIndex;
    }

    @Override
    public void writePayload(final PacketBuffer buffer)
    {
        buffer.writeVarInt(this.modeIndex);
    }

    @Override
    public void readPayload(final PacketBuffer buffer)
    {
        this.modeIndex = buffer.readVarInt();
    }

    @Override
    public void server(final ServerPlayerEntity playerEntity)
    {
        final ItemStack stack = ItemStackUtils.getModeItemStackFromPlayer(playerEntity);
        if (stack.getItem() instanceof IWithModeItem) {
            final IWithModeItem<?> modeItem = (IWithModeItem<?>) stack.getItem();
            modeItem.setMode(stack, modeIndex);
        }
    }
}
