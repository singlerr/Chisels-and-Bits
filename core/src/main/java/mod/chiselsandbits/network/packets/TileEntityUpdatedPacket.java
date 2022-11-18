package mod.chiselsandbits.network.packets;

import com.communi.suggestu.scena.core.dist.DistExecutor;
import mod.chiselsandbits.network.handlers.ClientPacketHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

import static com.communi.suggestu.scena.core.dist.Dist.CLIENT;

public final class TileEntityUpdatedPacket extends ModPacket
{

    private BlockPos blockPos;
    private CompoundTag updateData;

    public TileEntityUpdatedPacket(final BlockEntity tileEntity)
    {
        this.blockPos = tileEntity.getBlockPos();
        this.updateData = tileEntity.getUpdateTag();
    }

    public TileEntityUpdatedPacket(final FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(blockPos);
        buffer.writeNbt(updateData);
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
        this.blockPos = buffer.readBlockPos();
        this.updateData = buffer.readNbt();
    }

    @Override
    public void client()
    {
        DistExecutor.unsafeRunWhenOn(CLIENT, () -> () -> ClientPacketHandlers.handleTileEntityUpdatedPacket(blockPos, updateData));
    }
}
