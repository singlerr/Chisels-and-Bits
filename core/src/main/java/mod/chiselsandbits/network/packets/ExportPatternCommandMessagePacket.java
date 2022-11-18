package mod.chiselsandbits.network.packets;

import com.communi.suggestu.scena.core.dist.Dist;
import com.communi.suggestu.scena.core.dist.DistExecutor;
import mod.chiselsandbits.network.handlers.ClientPacketHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ExportPatternCommandMessagePacket extends ModPacket
{
    private BlockPos target;
    private String   name;

    public ExportPatternCommandMessagePacket(final BlockPos target, final String name)
    {
        this.target = target;
        this.name = name;
    }

    public ExportPatternCommandMessagePacket(final FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(target);
        buffer.writeUtf(name, 512);
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
        target = buffer.readBlockPos();
        name = buffer.readUtf(512);
    }

    @Override
    public void client()
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.handleExportPatternCommandMessage(target, name));
    }
}
