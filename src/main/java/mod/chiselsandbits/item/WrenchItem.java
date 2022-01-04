package mod.chiselsandbits.item;

import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.click.IRightClickControllingItem;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.api.modification.operation.IModificationOperation;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public class WrenchItem extends Item implements IWithModeItem<IModificationOperation>, IRightClickControllingItem
{
    private static final Logger LOGGER = LogManager.getLogger();

    public WrenchItem(final Properties properties)
    {
        super(properties);
    }

    @NotNull
    @Override
    public IModificationOperation getMode(final ItemStack stack)
    {
        final CompoundNBT stackNbt = stack.getOrCreateTag();
        if (stackNbt.contains(NbtConstants.MODIFICATION_MODE))
        {
            final String modeName = stackNbt.getString(NbtConstants.MODIFICATION_MODE);
            try {
                final IModificationOperation registryMode = IModificationOperation.getRegistry().getValue(new ResourceLocation(modeName));
                if (registryMode == null)
                    return IModificationOperation.getDefaultMode();

                return registryMode;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.error(String.format("An ItemStack got loaded with a name that is not a valid modification mode: %s", modeName));
                this.setMode(stack, IModificationOperation.getDefaultMode());
            }
        }

        return IModificationOperation.getDefaultMode();
    }

    @Override
    public void setMode(final ItemStack stack, final IModificationOperation mode)
    {
        if (mode == null)
            return;

        stack.getOrCreateTag().putString(NbtConstants.MODIFICATION_MODE, Objects.requireNonNull(mode.getRegistryName()).toString());
    }

    @NotNull
    @Override
    public Collection<IModificationOperation> getPossibleModes()
    {
        return IModificationOperation.getRegistry().getValues().stream().sorted(Comparator.comparing(((ForgeRegistry<IModificationOperation>) IModificationOperation.getRegistry())::getID)).collect(Collectors.toList());
    }

    @Override
    public boolean canUse(final PlayerEntity playerEntity)
    {
        final RayTraceResult result = RayTracingUtils.rayTracePlayer(playerEntity);
        if (!(result instanceof BlockRayTraceResult) || result.getType() != RayTraceResult.Type.BLOCK)
            return false;
        final BlockRayTraceResult blockHitResult = (BlockRayTraceResult) result;

        final TileEntity blockEntity = playerEntity.level.getBlockEntity(blockHitResult.getBlockPos());
        return (blockEntity instanceof IMultiStateBlockEntity);
    }

    @Override
    public ClickProcessingState handleRightClickProcessing(
      final PlayerEntity playerEntity, final Hand hand, final BlockPos position, final Direction face, final ClickProcessingState currentState)
    {
        final RayTraceResult result = RayTracingUtils.rayTracePlayer(playerEntity);
        if (!(result instanceof BlockRayTraceResult) || result.getType() != RayTraceResult.Type.BLOCK)
            return ClickProcessingState.DENIED;

        final BlockRayTraceResult blockHitResult = (BlockRayTraceResult) result;
        final TileEntity blockEntity = playerEntity.level.getBlockEntity(blockHitResult.getBlockPos());
        if (!(blockEntity instanceof IMultiStateBlockEntity))
            return ClickProcessingState.DENIED;

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) blockEntity;
        try(IBatchMutation ignored = multiStateBlockEntity.batch(IChangeTrackerManager.getInstance().getChangeTracker(playerEntity)))
        {
            getMode(playerEntity.getItemInHand(hand)).apply(multiStateBlockEntity);
        }

        return ClickProcessingState.ALLOW;
    }
}
