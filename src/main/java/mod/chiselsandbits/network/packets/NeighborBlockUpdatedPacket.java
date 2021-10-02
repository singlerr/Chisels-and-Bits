package mod.chiselsandbits.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class NeighborBlockUpdatedPacket extends ModPacket
{

    private BlockPos toUpdate = BlockPos.ZERO;
    private BlockPos from = BlockPos.ZERO;

    public NeighborBlockUpdatedPacket(final BlockPos toUpdate, final BlockPos from)
    {
        super();
        this.toUpdate = toUpdate;
        this.from = from;
    }

    public NeighborBlockUpdatedPacket(PacketBuffer buffer)
    {
        super();
        readPayload(buffer);
    }

    @Override
    public void writePayload(final PacketBuffer buffer)
    {
        buffer.writeBlockPos(this.toUpdate);
        buffer.writeBlockPos(this.from);
    }

    @Override
    public void readPayload(final PacketBuffer buffer)
    {
        this.toUpdate = buffer.readBlockPos();
        this.from = buffer.readBlockPos();
    }

    @Override
    public void client()
    {
        Minecraft.getInstance().level.getBlockState(toUpdate)
          .neighborChanged(
            Minecraft.getInstance().level,
            toUpdate,
            Minecraft.getInstance().level.getBlockState(from).getBlock(),
            from,
            false
          );
    }
}
