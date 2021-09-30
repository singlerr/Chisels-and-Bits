package mod.chiselsandbits.change.changes;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.utils.BitInventoryUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class BlockUpdatedChange implements IChange
{
    private final World world;
    private final PlayerEntity player;

    private final BlockPos blockPos;
    private final IMultiStateSnapshot before;
    private final IMultiStateSnapshot after;

    public BlockUpdatedChange(
      final World world,
      final PlayerEntity player,
      final BlockPos blockPos,
      final IMultiStateSnapshot before,
      final IMultiStateSnapshot after) {
        this.world = world;
        this.player = player;
        this.blockPos = blockPos;
        this.before = before;
        this.after = after;
    }

    @Override
    public boolean canUndo()
    {
        final TileEntity tileEntity = world.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
            return false;

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        return after.createNewShapeIdentifier().equals(multiStateBlockEntity.createNewShapeIdentifier()) && hasRequiredUndoBits();
    }

    @Override
    public boolean canRedo()
    {
        final TileEntity tileEntity = world.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
            return false;

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        return before.createNewShapeIdentifier().equals(multiStateBlockEntity.createNewShapeIdentifier()) && hasRequiredRedoBits();
    }

    @Override
    public void undo() throws IllegalChangeAttempt
    {
        if (!canUndo())
            throw new IllegalChangeAttempt();

        final TileEntity tileEntity = world.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
            throw new IllegalChangeAttempt();

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

        final IBitInventory bitInventory = IBitInventoryManager.getInstance().create(player);
        difference.forEach((state, diff) -> {
            if (diff < 0)
                bitInventory.extract(state, -diff);
            else
                BitInventoryUtils.insertIntoOrSpawn(player, state, diff);
        });
    }

    @Override
    public void redo() throws IllegalChangeAttempt
    {
        if (!canUndo())
            throw new IllegalChangeAttempt();

        final TileEntity tileEntity = world.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
            throw new IllegalChangeAttempt();

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

        final IBitInventory bitInventory = IBitInventoryManager.getInstance().create(player);
        difference.forEach((state, diff) -> {
            if (diff < 0)
                bitInventory.extract(state, -diff);
            else
                BitInventoryUtils.insertIntoOrSpawn(player, state, diff);
        });
    }

    private boolean hasRequiredUndoBits() {
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

    private boolean hasRequiredRedoBits() {
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
