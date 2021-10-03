package mod.chiselsandbits.change.changes;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.multistate.snapshot.EmptySnapshot;
import mod.chiselsandbits.utils.BitInventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.Optional;

public class BitChange implements IChange
{
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

    public BitChange(final INBT tag)
    {
        Validate.isInstanceOf(CompoundNBT.class, tag);
        this.deserializeNBT((CompoundNBT) tag);
    }

    @Override
    public boolean canUndo(final PlayerEntity player)
    {
        final TileEntity tileEntity = player.level.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity)) {
            final BlockState currentState = player.level.getBlockState(blockPos);
            return after.getStatics().getStateCounts().size() == 1 && after.getStatics().getStateCounts().getOrDefault(currentState, 0) == 4096;
        }

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        return after.createNewShapeIdentifier().equals(multiStateBlockEntity.createNewShapeIdentifier()) && hasRequiredUndoBits(player);
    }

    @Override
    public boolean canRedo(final PlayerEntity player)
    {
        final TileEntity tileEntity = player.level.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
        {
            final BlockState currentState = player.level.getBlockState(blockPos);
            return before.getStatics().getStateCounts().size() == 1 && before.getStatics().getStateCounts().getOrDefault(currentState, 0) == 4096;
        }

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        return before.createNewShapeIdentifier().equals(multiStateBlockEntity.createNewShapeIdentifier()) && hasRequiredRedoBits(player);
    }

    @Override
    public void undo(final PlayerEntity player) throws IllegalChangeAttempt
    {
        if (!canUndo(player))
            throw new IllegalChangeAttempt();

        TileEntity tileEntity = player.level.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity)) {
            BlockState currentState = player.level.getBlockState(blockPos);
            BlockState initializationState = currentState;
            final Optional<Block> convertedState = IConversionManager.getInstance().getChiseledVariantOf(currentState);
            if (!convertedState.isPresent())
                throw new IllegalChangeAttempt();

            player.level.setBlock(blockPos, convertedState.get().defaultBlockState(), Constants.BlockFlags.DEFAULT);
            tileEntity = player.level.getBlockEntity(blockPos);
            if (!(tileEntity instanceof IMultiStateBlockEntity))
                throw new IllegalChangeAttempt();
        }

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        final Map<BlockState, Integer> afterStates = after.getStatics().getStateCounts();
        final Map<BlockState, Integer> beforeStates = before.getStatics().getStateCounts();

        final Map<BlockState, Integer> difference = Maps.newHashMap();
        beforeStates.forEach((state, count) -> {
            difference.put(state, afterStates.getOrDefault(state, 0) - count);
        });
        afterStates.forEach((state, count) -> {
            if (!difference.containsKey(state))
                difference.put(state, count);
        });

        try(IBatchMutation batch = multiStateBlockEntity.batch()) {
            multiStateBlockEntity.initializeWith(Blocks.AIR.defaultBlockState());
            before.stream().forEach(
              iStateEntryInfo -> {
                  try
                  {
                      multiStateBlockEntity.setInAreaTarget(iStateEntryInfo.getState(), iStateEntryInfo.getStartPoint());
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
    public void redo(final PlayerEntity player) throws IllegalChangeAttempt
    {
        if (!canRedo(player))
            throw new IllegalChangeAttempt();

        TileEntity tileEntity = player.level.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity)) {
            BlockState currentState = player.level.getBlockState(blockPos);
            BlockState initializationState = currentState;
            if (currentState.isAir()) {
                currentState = Blocks.STONE.defaultBlockState();
            }

            final Optional<Block> convertedState = IConversionManager.getInstance().getChiseledVariantOf(currentState);
            if (!convertedState.isPresent())
                throw new IllegalChangeAttempt();

            player.level.setBlock(blockPos, convertedState.get().defaultBlockState(), Constants.BlockFlags.DEFAULT);
            tileEntity = player.level.getBlockEntity(blockPos);
            if (!(tileEntity instanceof IMultiStateBlockEntity))
                throw new IllegalChangeAttempt();
        }

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        final Map<BlockState, Integer> afterStates = after.getStatics().getStateCounts();
        final Map<BlockState, Integer> beforeStates = before.getStatics().getStateCounts();

        final Map<BlockState, Integer> difference = Maps.newHashMap();
        beforeStates.forEach((state, count) -> {
            difference.put(state, count - afterStates.getOrDefault(state, 0));
        });
        afterStates.forEach((state, count) -> {
            if (!difference.containsKey(state))
                difference.put(state, -count);
        });

        try(IBatchMutation batch = multiStateBlockEntity.batch()) {
            multiStateBlockEntity.initializeWith(Blocks.AIR.defaultBlockState());
            after.stream().forEach(
              s -> {
                  try
                  {
                      multiStateBlockEntity.setInAreaTarget(s.getState(), s.getStartPoint());
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

    private boolean hasRequiredUndoBits(final PlayerEntity player) {
        if (player.isCreative())
            return true;

        final Map<BlockState, Integer> afterStates = after.getStatics().getStateCounts();
        final Map<BlockState, Integer> beforeStates = before.getStatics().getStateCounts();

        final Map<BlockState, Integer> difference = Maps.newHashMap();
        beforeStates.forEach((state, count) -> {
            difference.put(state, afterStates.getOrDefault(state, 0) - count);
        });
        afterStates.forEach((state, count) -> {
            if (!difference.containsKey(state))
                difference.put(state, count);
        });

        final IBitInventory bitInventory = IBitInventoryManager.getInstance().create(player);
        return difference.entrySet().stream()
                 .filter(e -> e.getValue() < 0)
                 .allMatch(e -> bitInventory.canExtract(e.getKey(), -e.getValue()));
    }

    private boolean hasRequiredRedoBits(final PlayerEntity player) {
        if (player.isCreative())
            return  true;

        final Map<BlockState, Integer> afterStates = after.getStatics().getStateCounts();
        final Map<BlockState, Integer> beforeStates = before.getStatics().getStateCounts();

        final Map<BlockState, Integer> difference = Maps.newHashMap();
        beforeStates.forEach((state, count) -> {
            difference.put(state, count - afterStates.getOrDefault(state, 0));
        });
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
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = new CompoundNBT();

        tag.put("pos", NBTUtil.writeBlockPos(this.blockPos));
        tag.put("before", this.before.toItemStack().toBlockStack().serializeNBT());
        tag.put("after", this.after.toItemStack().toBlockStack().serializeNBT());

        return tag;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        this.blockPos = NBTUtil.readBlockPos(nbt.getCompound("pos"));
        this.before = deserializeSnapshot(nbt.getCompound("before"));
        this.after = deserializeSnapshot(nbt.getCompound("after"));
    }

    private static IMultiStateSnapshot deserializeSnapshot(final CompoundNBT nbt)  {
        final ItemStack stack = ItemStack.of(nbt);
        if (stack.isEmpty())
            return EmptySnapshot.INSTANCE;

        if (!(stack.getItem() instanceof IMultiStateItem))
            return EmptySnapshot.INSTANCE;

        return ((IMultiStateItem) stack.getItem()).createItemStack(stack).createSnapshot();
    }
}
