package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.network.handlers.ClientPacketHandlers;
import mod.chiselsandbits.platforms.core.dist.Dist;
import mod.chiselsandbits.platforms.core.dist.DistExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ImportPatternCommandMessagePacket extends ModPacket
{
    private String   name;

    public ImportPatternCommandMessagePacket(final String name)
    {
        this.name = name;
    }

    public ImportPatternCommandMessagePacket(final FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        buffer.writeUtf(name, 512);
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
        name = buffer.readUtf(512);
    }

    @Override
    public void client()
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.handleImportPatternCommandMessage(name));
    }
}
