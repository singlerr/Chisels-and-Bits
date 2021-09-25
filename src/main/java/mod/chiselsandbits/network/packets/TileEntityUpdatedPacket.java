package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.network.handlers.ClientPacketHandlers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import static net.minecraftforge.api.distmarker.Dist.CLIENT;

public final class TileEntityUpdatedPacket extends ModPacket
{

    private BlockPos blockPos;
    private CompoundNBT updateData;

    public TileEntityUpdatedPacket(final TileEntity tileEntity)
    {
        this.blockPos = tileEntity.getBlockPos();
        this.updateData = tileEntity.getUpdateTag();
    }

    public TileEntityUpdatedPacket(final PacketBuffer buffer)
    {
        super(buffer);
    }

    @Override
    public void writePayload(final PacketBuffer buffer)
    {
        buffer.writeBlockPos(blockPos);
        buffer.writeNbt(updateData);
    }

    @Override
    public void readPayload(final PacketBuffer buffer)
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
