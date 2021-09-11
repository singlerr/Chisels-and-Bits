package mod.chiselsandbits.network.handlers;

import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.profiling.ProfilingManager;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.DistExecutor;

public final class ClientPacketHandlers
{

    private ClientPacketHandlers()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ClientPacketHandlers. This is a utility class");
    }

    public static void handleTileEntityUpdatedPacket(final BlockPos blockPos, final CompoundNBT updateTag) {
        if (Minecraft.getInstance().world != null) {
            final TileEntity tileEntity = Minecraft.getInstance().world.getTileEntity(blockPos);
            if (tileEntity != null && tileEntity.getWorld() != null) {

                try(IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Handling tile entity update packet"))
                {
                    try(IProfilerSection ignored2 = ProfilingManager.getInstance().withSection("Updating tile entity"))
                    {
                        tileEntity.handleUpdateTag(Minecraft.getInstance().world.getBlockState(blockPos), updateTag);
                    }

                    try(IProfilerSection ignored2 = ProfilingManager.getInstance().withSection("Scheduling refresh"))
                    {
                        tileEntity.getWorld().notifyBlockUpdate(
                          tileEntity.getPos(),
                          Blocks.AIR.getDefaultState(),
                          tileEntity.getBlockState(),
                          Constants.BlockFlags.DEFAULT_AND_RERENDER
                        );
                    }
                }
            }
        }
    }
}
