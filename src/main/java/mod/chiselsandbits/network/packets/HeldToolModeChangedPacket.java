package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;

public final class HeldToolModeChangedPacket extends ModPacket
{

    private int modeIndex;

    public HeldToolModeChangedPacket(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    public HeldToolModeChangedPacket(final int modeIndex)
    {
        this.modeIndex = modeIndex;
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(this.modeIndex);
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
        this.modeIndex = buffer.readVarInt();
    }

    @Override
    public void server(final ServerPlayer playerEntity)
    {
        final ItemStack stack = ItemStackUtils.getModeItemStackFromPlayer(playerEntity);
        if (stack.getItem() instanceof IWithModeItem) {
            final IWithModeItem<?> modeItem = (IWithModeItem<?>) stack.getItem();
            modeItem.setMode(stack, modeIndex);
        }
    }
}
