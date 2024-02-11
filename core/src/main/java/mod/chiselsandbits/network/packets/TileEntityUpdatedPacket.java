package mod.chiselsandbits.network.packets;

import com.communi.suggestu.scena.core.dist.DistExecutor;
import io.netty.buffer.Unpooled;
import mod.chiselsandbits.api.block.entity.INetworkUpdatableEntity;
import mod.chiselsandbits.network.handlers.ClientPacketHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.communi.suggestu.scena.core.dist.Dist.CLIENT;

public final class TileEntityUpdatedPacket extends ModPacket
{

    private BlockPos blockPos;
    private byte[] data;
    private Consumer<byte[]> dataConsumer;

    public TileEntityUpdatedPacket(final INetworkUpdatableEntity tileEntity)
    {
        this.blockPos = tileEntity.getBlockPos();
        this.data = writeBlockEntity(tileEntity);
    }

    private static byte @NotNull [] writeBlockEntity(INetworkUpdatableEntity tileEntity) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        tileEntity.serializeInto(buf);
        byte[] data = buf.array();
        buf.release();
        return data;
    }

    public TileEntityUpdatedPacket(final FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(blockPos);
        buffer.writeByteArray(data);
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
        this.blockPos = buffer.readBlockPos();
        this.data = buffer.readByteArray();
    }

    @Override
    public void client()
    {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        DistExecutor.runWhenOn(CLIENT, () -> () -> ClientPacketHandlers.handleTileEntityUpdatedPacket(blockPos, buf));
        buf.release();
    }
}
