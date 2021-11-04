package mod.chiselsandbits.network.handlers;

import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.client.screen.AbstractChiselsAndBitsScreen;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.client.screens.widgets.ChangeTrackerOperationsWidget;
import mod.chiselsandbits.profiling.ProfilingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Constants;

import java.util.Optional;

public final class ClientPacketHandlers
{

    private ClientPacketHandlers()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ClientPacketHandlers. This is a utility class");
    }

    public static void handleTileEntityUpdatedPacket(final BlockPos blockPos, final CompoundTag updateTag) {
        if (Minecraft.getInstance().level != null) {
            BlockEntity tileEntity = Minecraft.getInstance().level.getBlockEntity(blockPos);
            if (!(tileEntity instanceof IMultiStateBlockEntity)) {
                BlockState currentState = Minecraft.getInstance().level.getBlockState(blockPos);
                BlockState initializationState = currentState;
                if (currentState.isAir()) {
                    currentState = Blocks.STONE.defaultBlockState();
                }

                final Optional<Block> convertedState = IConversionManager.getInstance().getChiseledVariantOf(currentState);
                if (!convertedState.isPresent())
                    return;

                Minecraft.getInstance().level.setBlock(blockPos, convertedState.get().defaultBlockState(), Constants.BlockFlags.DEFAULT);
                tileEntity = Minecraft.getInstance().level.getBlockEntity(blockPos);
                if (!(tileEntity instanceof IMultiStateBlockEntity))
                    return;
            }

            if (tileEntity != null && tileEntity.getLevel() != null) {

                try(IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Handling tile entity update packet"))
                {
                    try(IProfilerSection ignored2 = ProfilingManager.getInstance().withSection("Updating tile entity"))
                    {
                        tileEntity.handleUpdateTag(updateTag);
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

    public static void handleChangeTrackerUpdated(final CompoundTag tag) {
        IChangeTrackerManager.getInstance().getChangeTracker(Minecraft.getInstance().player).deserializeNBT(tag);
        if(Minecraft.getInstance().screen instanceof AbstractChiselsAndBitsScreen)
        {
            ((AbstractChiselsAndBitsScreen) Minecraft.getInstance().screen).getWidgets()
              .stream()
              .filter(ChangeTrackerOperationsWidget.class::isInstance)
              .map(ChangeTrackerOperationsWidget.class::cast)
              .forEach(ChangeTrackerOperationsWidget::updateState);
        }
    }

    public static void handleNeighborUpdated(final BlockPos toUpdate, final BlockPos from) {
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
