package mod.chiselsandbits.change.changes;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.util.IBatchMutation;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.variant.state.IStateVariant;
import mod.chiselsandbits.api.variant.state.IStateVariantManager;
import mod.chiselsandbits.multistate.snapshot.EmptySnapshot;
import mod.chiselsandbits.utils.BitInventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.Optional;

public class BitChange implements IChange
{
    private CompoundTag lazyLoadingTag;

    private BlockPos blockPos;
    private IMultiStateSnapshot before;
    private IMultiStateSnapshot after;

    public BitChange(
      final BlockPos blockPos,
      final IMultiStateSnapshot before,
      final IMultiStateSnapshot after) {
        this.blockPos = blockPos;
        this.before = before;
        this.after = after;
    }

    public BitChange(final Tag tag)
    {
        Validate.isInstanceOf(CompoundTag.class, tag);
        this.lazyLoadingTag = (CompoundTag) tag;
    }

    private void load() {
        if (lazyLoadingTag == null) {
            return;
        }

        this.deserializeNBT(lazyLoadingTag);
        lazyLoadingTag = null;
    }

    @Override
    public boolean canUndo(final Player player)
    {
        load();

        final BlockEntity tileEntity = player.level.getBlockEntity(blockPos);
        if (!(tileEntity instanceof final IMultiStateBlockEntity multiStateBlockEntity)) {
            final BlockState state = player.level.getBlockState(blockPos);
            final Optional<IStateVariant> additionalStateInfo = IStateVariantManager.getInstance().getStateVariant(
              state, Optional.ofNullable(player.level.getBlockEntity(blockPos))
            );
            final BlockInformation currentState = new BlockInformation(state, additionalStateInfo);

            return after.getStatics().getStateCounts().size() == 1 && after.getStatics().getStateCounts().getOrDefault(currentState, 0) == 4096;
        }

        return after.createNewShapeIdentifier().equals(multiStateBlockEntity.createNewShapeIdentifier()) && hasRequiredUndoBits(player);
    }

    @Override
    public boolean canRedo(final Player player)
    {
        load();

        final BlockEntity tileEntity = player.level.getBlockEntity(blockPos);
        if (!(tileEntity instanceof final IMultiStateBlockEntity multiStateBlockEntity))
        {
            final BlockState state = player.level.getBlockState(blockPos);
            final Optional<IStateVariant> additionalStateInfo = IStateVariantManager.getInstance().getStateVariant(
              state, Optional.ofNullable(player.level.getBlockEntity(blockPos))
            );
            final BlockInformation currentState = new BlockInformation(state, additionalStateInfo);

            return before.getStatics().getStateCounts().size() == 1 && before.getStatics().getStateCounts().getOrDefault(currentState, 0) == 4096;
        }

        return before.createNewShapeIdentifier().equals(multiStateBlockEntity.createNewShapeIdentifier()) && hasRequiredRedoBits(player);
    }

    @Override
    public void undo(final Player player) throws IllegalChangeAttempt
    {
        load();

        if (!canUndo(player))
            throw new IllegalChangeAttempt();

        BlockEntity tileEntity = player.level.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity)) {
            BlockState currentState = player.level.getBlockState(blockPos);
            final Optional<Block> convertedState = IConversionManager.getInstance().getChiseledVariantOf(currentState);
            if (convertedState.isEmpty())
                throw new IllegalChangeAttempt();

            player.level.setBlock(blockPos, convertedState.get().defaultBlockState(), Block.UPDATE_ALL);
            tileEntity = player.level.getBlockEntity(blockPos);
            if (!(tileEntity instanceof IMultiStateBlockEntity))
                throw new IllegalChangeAttempt();
        }

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        final Map<IBlockInformation, Integer> afterStates = after.getStatics().getStateCounts();
        final Map<IBlockInformation, Integer> beforeStates = before.getStatics().getStateCounts();

        final Map<IBlockInformation, Integer> difference = Maps.newHashMap();
        beforeStates.forEach((state, count) -> difference.put(state, afterStates.getOrDefault(state, 0) - count));
        afterStates.forEach((state, count) -> {
            if (!difference.containsKey(state))
                difference.put(state, count);
        });

        try(IBatchMutation batch = multiStateBlockEntity.batch()) {
            multiStateBlockEntity.initializeWith(BlockInformation.AIR);
            before.stream().forEach(
              iStateEntryInfo -> {
                  try
                  {
                      multiStateBlockEntity.setInAreaTarget(iStateEntryInfo.getBlockInformation(), iStateEntryInfo.getStartPoint());
                  }
                  catch (SpaceOccupiedException e)
                  {
                      //Noop
                  }
              }
            );
        }

        if (!player.isCreative()) {
            final IBitInventory bitInventory = IBitInventoryManager.getInstance().create(player);
            difference.forEach((state, diff) -> {
                if (state.isAir())
                    return;

                if (diff < 0)
                    bitInventory.extract(state, -diff);
                else
                    BitInventoryUtils.insertIntoOrSpawn(player, state, diff);
            });
        }
    }

    @Override
    public void redo(final Player player) throws IllegalChangeAttempt
    {
        load();

        if (!canRedo(player))
            throw new IllegalChangeAttempt();

        BlockEntity tileEntity = player.level.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity)) {
            BlockState currentState = player.level.getBlockState(blockPos);
            BlockState initializationState = currentState;
            if (currentState.isAir()) {
                currentState = Blocks.STONE.defaultBlockState();
            }

            final Optional<Block> convertedState = IConversionManager.getInstance().getChiseledVariantOf(currentState);
            if (convertedState.isEmpty())
                throw new IllegalChangeAttempt();

            player.level.setBlock(blockPos, convertedState.get().defaultBlockState(), Block.UPDATE_ALL);
            tileEntity = player.level.getBlockEntity(blockPos);
            if (!(tileEntity instanceof IMultiStateBlockEntity))
                throw new IllegalChangeAttempt();
        }

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        final Map<IBlockInformation, Integer> afterStates = after.getStatics().getStateCounts();
        final Map<IBlockInformation, Integer> beforeStates = before.getStatics().getStateCounts();

        final Map<IBlockInformation, Integer> difference = Maps.newHashMap();
        beforeStates.forEach((state, count) -> difference.put(state, count - afterStates.getOrDefault(state, 0)));
        afterStates.forEach((state, count) -> {
            if (!difference.containsKey(state))
                difference.put(state, -count);
        });

        try(IBatchMutation batch = multiStateBlockEntity.batch()) {
            multiStateBlockEntity.initializeWith(BlockInformation.AIR);
            after.stream().forEach(
              s -> {
                  try
                  {
                      multiStateBlockEntity.setInAreaTarget(s.getBlockInformation(), s.getStartPoint());
                  }
                  catch (SpaceOccupiedException e)
                  {
                      //Noop
                  }
              }
            );
        }

        if (!player.isCreative())
        {
            final IBitInventory bitInventory = IBitInventoryManager.getInstance().create(player);
            difference.forEach((state, diff) -> {
                if (diff < 0)
                    bitInventory.extract(state, -diff);
                else
                    BitInventoryUtils.insertIntoOrSpawn(player, state, diff);
            });
        }
    }

    private boolean hasRequiredUndoBits(final Player player) {
        if (player.isCreative())
            return true;

        final Map<IBlockInformation, Integer> afterStates = after.getStatics().getStateCounts();
        final Map<IBlockInformation, Integer> beforeStates = before.getStatics().getStateCounts();

        final Map<IBlockInformation, Integer> difference = Maps.newHashMap();
        beforeStates.forEach((state, count) -> difference.put(state, afterStates.getOrDefault(state, 0) - count));
        afterStates.forEach((state, count) -> {
            if (!difference.containsKey(state))
                difference.put(state, count);
        });

        final IBitInventory bitInventory = IBitInventoryManager.getInstance().create(player);
        return difference.entrySet().stream()
                 .filter(e -> e.getValue() < 0)
                 .allMatch(e -> bitInventory.canExtract(e.getKey(), -e.getValue()));
    }

    private boolean hasRequiredRedoBits(final Player player) {
        if (player.isCreative())
            return  true;

        final Map<IBlockInformation, Integer> afterStates = after.getStatics().getStateCounts();
        final Map<IBlockInformation, Integer> beforeStates = before.getStatics().getStateCounts();

        final Map<IBlockInformation, Integer> difference = Maps.newHashMap();
        beforeStates.forEach((state, count) -> difference.put(state, count - afterStates.getOrDefault(state, 0)));
        afterStates.forEach((state, count) -> {
            if (!difference.containsKey(state))
                difference.put(state, -count);
        });

        final IBitInventory bitInventory = IBitInventoryManager.getInstance().create(player);
        return difference.entrySet().stream()
          .filter(e -> e.getValue() < 0)
          .allMatch(e -> bitInventory.canExtract(e.getKey(), -e.getValue()));
    }

    @Override
    public CompoundTag serializeNBT()
    {
        if(lazyLoadingTag != null)
            return lazyLoadingTag.copy();

        final CompoundTag tag = new CompoundTag();

        tag.put("pos", NbtUtils.writeBlockPos(this.blockPos));
        tag.put("before", this.before.toItemStack().toBlockStack().save(new CompoundTag()));
        tag.put("after", this.after.toItemStack().toBlockStack().save(new CompoundTag()));

        return tag;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.blockPos = NbtUtils.readBlockPos(nbt.getCompound("pos"));
        this.before = deserializeSnapshot(nbt.getCompound("before"));
        this.after = deserializeSnapshot(nbt.getCompound("after"));
    }

    private static IMultiStateSnapshot deserializeSnapshot(final CompoundTag nbt)  {
        final ItemStack stack = ItemStack.of(nbt);
        if (stack.isEmpty())
            return EmptySnapshot.INSTANCE;

        if (!(stack.getItem() instanceof IMultiStateItem))
            return EmptySnapshot.INSTANCE;

        return ((IMultiStateItem) stack.getItem()).createItemStack(stack).createSnapshot();
    }
}
