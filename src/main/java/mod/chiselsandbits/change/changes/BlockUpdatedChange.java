package mod.chiselsandbits.change.changes;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.utils.BitInventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Constants;

import java.util.Map;
import java.util.Optional;

public class BlockUpdatedChange implements IChange
{
    private final BlockPos blockPos;
    private final IMultiStateSnapshot before;
    private final IMultiStateSnapshot after;

    public BlockUpdatedChange(
      final BlockPos blockPos,
      final IMultiStateSnapshot before,
      final IMultiStateSnapshot after) {
        this.blockPos = blockPos;
        this.before = before;
        this.after = after;
    }

    @Override
    public boolean canUndo(final Player player)
    {
        final BlockEntity tileEntity = player.level.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity)) {
            final BlockState currentState = player.level.getBlockState(blockPos);
            return after.getStatics().getStateCounts().size() == 1 && after.getStatics().getStateCounts().getOrDefault(currentState, 0) == 4096;
        }

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        return after.createNewShapeIdentifier().equals(multiStateBlockEntity.createNewShapeIdentifier()) && hasRequiredUndoBits(player);
    }

    @Override
    public boolean canRedo(final Player player)
    {
        final BlockEntity tileEntity = player.level.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
        {
            final BlockState currentState = player.level.getBlockState(blockPos);
            return before.getStatics().getStateCounts().size() == 1 && before.getStatics().getStateCounts().getOrDefault(currentState, 0) == 4096;
        }

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        return before.createNewShapeIdentifier().equals(multiStateBlockEntity.createNewShapeIdentifier()) && hasRequiredRedoBits(player);
    }

    @Override
    public void undo(final Player player) throws IllegalChangeAttempt
    {
        if (!canUndo(player))
            throw new IllegalChangeAttempt();

        BlockEntity tileEntity = player.level.getBlockEntity(blockPos);
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

            ((IMultiStateBlockEntity) tileEntity).initializeWith(initializationState);
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
    public void redo(final Player player) throws IllegalChangeAttempt
    {
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
            if (!convertedState.isPresent())
                throw new IllegalChangeAttempt();

            player.level.setBlock(blockPos, convertedState.get().defaultBlockState(), Constants.BlockFlags.DEFAULT);
            tileEntity = player.level.getBlockEntity(blockPos);
            if (!(tileEntity instanceof IMultiStateBlockEntity))
                throw new IllegalChangeAttempt();

            ((IMultiStateBlockEntity) tileEntity).initializeWith(initializationState);
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

    private boolean hasRequiredUndoBits(final Player player) {
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

    private boolean hasRequiredRedoBits(final Player player) {
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
}
