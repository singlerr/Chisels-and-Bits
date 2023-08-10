package mod.chiselsandbits.network.handlers;

import com.mojang.datafixers.util.Either;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.block.entity.INetworkUpdateableEntity;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.client.screen.AbstractChiselsAndBitsScreen;
import mod.chiselsandbits.api.client.sharing.IPatternSharingManager;
import mod.chiselsandbits.api.client.sharing.PatternIOException;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.client.screens.widgets.ChangeTrackerOperationsWidget;
import mod.chiselsandbits.clipboard.CreativeClipboardManager;
import mod.chiselsandbits.clipboard.CreativeClipboardUtils;
import mod.chiselsandbits.item.multistate.SingleBlockMultiStateItemStack;
import mod.chiselsandbits.network.packets.GivePlayerPatternCommandPacket;
import mod.chiselsandbits.profiling.ProfilingManager;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
                if (convertedState.isEmpty())
                    return;

                Minecraft.getInstance().level.setBlock(blockPos, convertedState.get().defaultBlockState(), Block.UPDATE_NONE);
                tileEntity = Minecraft.getInstance().level.getBlockEntity(blockPos);
                if (!(tileEntity instanceof IMultiStateBlockEntity))
                    return;
            }

            if (tileEntity != null && tileEntity.getLevel() != null) {

                try(IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Handling tile entity update packet"))
                {
                    try(IProfilerSection ignored2 = ProfilingManager.getInstance().withSection("Updating tile entity"))
                    {
                        if (tileEntity instanceof INetworkUpdateableEntity networkUpdateableEntity) {
                            networkUpdateableEntity.handleUpdateTag(updateTag);
                        }
                        else
                        {
                            tileEntity.load(updateTag);

                            try(IProfilerSection ignored3 = ProfilingManager.getInstance().withSection("Scheduling refresh"))
                            {
                                tileEntity.getLevel().sendBlockUpdated(
                                  tileEntity.getBlockPos(),
                                  Blocks.AIR.defaultBlockState(),
                                  tileEntity.getBlockState(),
                                  Block.UPDATE_ALL
                                );
                            }
                        }
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

    public static void handleAddMultiStateToClipboard(final ItemStack stack) {
        final IMultiStateItemStack itemStack = new SingleBlockMultiStateItemStack(stack);
        CreativeClipboardUtils.addBrokenBlock(itemStack);
    }

    public static void handleExportPatternCommandMessage(final BlockPos target, final String name) {
        final BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(target);
        if (!(blockEntity instanceof IMultiStateBlockEntity multiStateBlockEntity))
        {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Failed to export pattern: " + name + " - Not a multistate block."));
            return;
        }

        final IMultiStateItemStack multiStateItemStack = multiStateBlockEntity.createSnapshot().toItemStack();
        IPatternSharingManager.getInstance().exportPattern(multiStateItemStack, name);
    }

    public static void handleImportPatternCommandMessage(final String name) {;
        final Either<IMultiStateItemStack, PatternIOException> importResult = IPatternSharingManager.getInstance().importPattern(name);
        importResult.ifLeft(stack -> {
            ChiselsAndBits.getInstance().getNetworkChannel()
              .sendToServer(new GivePlayerPatternCommandPacket(stack.toPatternStack()));
        });
        importResult.ifRight(e -> {
            Minecraft.getInstance().player.sendSystemMessage(e.getErrorMessage());
        });
    }
}
