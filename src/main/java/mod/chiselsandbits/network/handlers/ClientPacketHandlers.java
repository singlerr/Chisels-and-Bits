package mod.chiselsandbits.network.handlers;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.DistExecutor;

public final class ClientPacketHandlers
{

    private ClientPacketHandlers()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ClientPacketHandlers. This is a utility class");
    }

    public static DistExecutor.SafeRunnable getTileEntityUpdatePacketHandler(final BlockPos blockPos, final CompoundNBT compoundNBT) {
        return () -> handleTileEntityUpdatedPacket(blockPos, compoundNBT);
    }

    public static void handleTileEntityUpdatedPacket(final BlockPos blockPos, final CompoundNBT updateTag) {
        if (Minecraft.getInstance().world != null) {
            final TileEntity tileEntity = Minecraft.getInstance().world.getTileEntity(blockPos);
            if (tileEntity != null && tileEntity.getWorld() != null) {
                tileEntity.handleUpdateTag(Minecraft.getInstance().world.getBlockState(blockPos), updateTag);
                tileEntity.getWorld().markBlockRangeForRenderUpdate(
                  tileEntity.getPos(),
                  Blocks.AIR.getDefaultState(),
                  tileEntity.getBlockState()
                );
            }
        }
    }
}
