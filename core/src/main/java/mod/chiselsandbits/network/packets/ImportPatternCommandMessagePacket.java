package mod.chiselsandbits.network.packets;

import com.communi.suggestu.scena.core.dist.Dist;
import com.communi.suggestu.scena.core.dist.DistExecutor;
import mod.chiselsandbits.network.handlers.ClientPacketHandlers;
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
