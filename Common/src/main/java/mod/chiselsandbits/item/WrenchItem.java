package mod.chiselsandbits.item;

import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.click.IRightClickControllingItem;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.api.modification.operation.IModificationOperation;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.platforms.core.util.constants.NbtConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
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
        final CompoundTag stackNbt = stack.getOrCreateTag();
        if (stackNbt.contains(NbtConstants.MODIFICATION_MODE))
        {
            final String modeName = stackNbt.getString(NbtConstants.MODIFICATION_MODE);
            try {
                final Optional<IModificationOperation> registryMode = IModificationOperation.getRegistry().get(new ResourceLocation(modeName));
                return registryMode.orElseGet(IModificationOperation::getDefaultMode);
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
        {
            return;
        }

        stack.getOrCreateTag().putString(NbtConstants.MODIFICATION_MODE, Objects.requireNonNull(mode.getRegistryName()).toString());
    }

    @NotNull
    @Override
    public Collection<IModificationOperation> getPossibleModes()
    {
        return IModificationOperation.getRegistry().getValues().stream().sorted(Comparator.comparing(IModificationOperation::getRegistryName)).collect(Collectors.toList());
    }

    @Override
    public boolean canUse(final Player playerEntity)
    {
        final HitResult result = RayTracingUtils.rayTracePlayer(playerEntity);
        if (!(result instanceof final BlockHitResult blockHitResult) || result.getType() != HitResult.Type.BLOCK)
            return false;

        final BlockEntity blockEntity = playerEntity.level.getBlockEntity(blockHitResult.getBlockPos());
        return (blockEntity instanceof IMultiStateBlockEntity);
    }

    @Override
    public ClickProcessingState handleRightClickProcessing(
      final Player playerEntity, final InteractionHand hand, final BlockPos position, final Direction face, final ClickProcessingState currentState)
    {
        final HitResult result = RayTracingUtils.rayTracePlayer(playerEntity);
        if (!(result instanceof final BlockHitResult blockHitResult) || result.getType() != HitResult.Type.BLOCK)
            return ClickProcessingState.DENIED;

        final BlockEntity blockEntity = playerEntity.level.getBlockEntity(blockHitResult.getBlockPos());
        if (!(blockEntity instanceof final IMultiStateBlockEntity multiStateBlockEntity))
            return ClickProcessingState.DENIED;

        try(IBatchMutation ignored = multiStateBlockEntity.batch(IChangeTrackerManager.getInstance().getChangeTracker(playerEntity)))
        {
            getMode(playerEntity.getItemInHand(hand)).apply(multiStateBlockEntity);
        }

        return ClickProcessingState.ALLOW;
    }
}
