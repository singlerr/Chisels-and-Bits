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
        if (Minecraft.getInstance().level != null) {
            final TileEntity tileEntity = Minecraft.getInstance().level.getBlockEntity(blockPos);
            if (tileEntity != null && tileEntity.getLevel() != null) {

                try(IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Handling tile entity update packet"))
                {
                    try(IProfilerSection ignored2 = ProfilingManager.getInstance().withSection("Updating tile entity"))
                    {
                        tileEntity.handleUpdateTag(Minecraft.getInstance().level.getBlockState(blockPos), updateTag);
                    }

                    try(IProfilerSection ignored2 = ProfilingManager.getInstance().withSection("Scheduling refresh"))
                    {
                        tileEntity.getLevel().sendBlockUpdated(
                          tileEntity.getBlockPos(),
                          Blocks.AIR.defaultBlockState(),
                          tileEntity.getBlockState(),
                          Constants.BlockFlags.DEFAULT_AND_RERENDER
                        );
                    }
                }
            }
        }
    }
}
