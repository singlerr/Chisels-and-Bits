package mod.chiselsandbits.change.changes;

import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.InventoryUtils;
import mod.chiselsandbits.utils.BitInventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Constants;

import java.util.Optional;

public class BlockPlacedChange implements IChange
{
    private final Level    world;
    private final Player     player;
    private final BlockPos            blockPos;
    private final BlockState          initialState;
    private final IMultiStateSnapshot resultingSnapshot;
    private final boolean bitBased;

    public BlockPlacedChange(
      final Level world,
      final Player player,
      final BlockPos blockPos,
      final BlockState initialState,
      final IMultiStateSnapshot resultingSnapshot,
      final boolean bitBased) {
        this.world = world;
        this.player = player;
        this.blockPos = blockPos;
        this.initialState = initialState;
        this.resultingSnapshot = resultingSnapshot;
        this.bitBased = bitBased;
    }

    @Override
    public boolean canRedo()
    {
        if (!hasSnapshotStackOrBits())
            return false;

        return world.getBlockState(blockPos) == initialState;
    }

    @Override
    public boolean canUndo()
    {
        final BlockEntity tileEntity = world.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
            return false;

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        return resultingSnapshot.createNewShapeIdentifier().equals(multiStateBlockEntity.createNewShapeIdentifier());
    }

    @Override
    public void redo() throws IllegalChangeAttempt
    {
        if (!canRedo())
            throw new IllegalChangeAttempt();

        final BlockState primaryState = this.resultingSnapshot.getStatics().getPrimaryState();
        final Optional<BlockState> placeState = IConversionManager.getInstance().getChiseledVariantOf(primaryState).map(Block::defaultBlockState);
        if (!placeState.isPresent())
            throw new IllegalChangeAttempt();

        world.setBlock(blockPos, placeState.get(), Constants.BlockFlags.DEFAULT);
        final BlockEntity tileEntity = world.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity)) {
            world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
            throw new IllegalChangeAttempt();
        }

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        multiStateBlockEntity.initializeWith(Blocks.AIR.defaultBlockState());
        //noinspection unused -> Batched try-with-resources.
        try(IBatchMutation batch = multiStateBlockEntity.batch())
        {
            this.resultingSnapshot.stream().forEach(
              s -> {
                  try
                  {
                      multiStateBlockEntity.setInAreaTarget(
                        s.getState(),
                        s.getStartPoint()
                      );
                  }
                  catch (SpaceOccupiedException e)
                  {
                      //Noop we just set everything to air...
                  }
              }
            );
        }

        extractShapshotStackOrBits();
    }

    @Override
    public void undo() throws IllegalChangeAttempt
    {
        if (!canUndo())
            throw new IllegalChangeAttempt();

        world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
        insertSnapshotStackOrBits();
    }

    private boolean hasSnapshotStackOrBits() {
        if (!bitBased)
            return InventoryUtils.getChiseledStackMatchingSnapshot(player, resultingSnapshot).isEmpty();

        final IBitInventory playerInventory = IBitInventoryManager.getInstance().create(player);
        if (!initialState.isAir())
        {
            return  resultingSnapshot.getStatics().getStateCounts().entrySet().stream()
              .filter(e -> !e.getKey().isAir())
              .allMatch(e -> playerInventory.canExtract(e.getKey(), e.getValue()));
        }

        return resultingSnapshot.getStatics().getStateCounts().entrySet().stream()
          .filter(e -> !e.getKey().isAir())
          .allMatch(e -> {
              if (e.getKey() != initialState)
              {
                  return playerInventory.canExtract(e.getKey(), e.getValue());
              }

              return true;
         });
    }

    private void extractShapshotStackOrBits() {
        if (!bitBased)
        {
            InventoryUtils.extractChiseledStackMatchingSnapshot(player, resultingSnapshot);
            return;
        }

        final IBitInventory playerInventory = IBitInventoryManager.getInstance().create(player);
        if (!initialState.isAir())
        {
            resultingSnapshot.getStatics().getStateCounts().entrySet().stream()
              .filter(e -> !e.getKey().isAir())
              .forEach(e -> playerInventory.extract(e.getKey(), e.getValue()));
        }

        resultingSnapshot.getStatics().getStateCounts().entrySet().stream()
          .filter(e -> !e.getKey().isAir())
          .forEach(e -> {
              if (e.getKey() != initialState)
              {
                  playerInventory.extract(e.getKey(), e.getValue());
              }
              else
              {
                  BitInventoryUtils.insertIntoOrSpawn(player, e.getKey(), StateEntrySize.current().getBitsPerBlock() - e.getValue());
              }
          });
    }

    private void insertSnapshotStackOrBits() {
        if (!bitBased) {
            player.getInventory().add(resultingSnapshot.toItemStack().toBlockStack());
            return;
        }

        resultingSnapshot.getStatics().getStateCounts().entrySet().stream()
          .filter(e -> !e.getKey().isAir())
          .forEach(e -> BitInventoryUtils.insertIntoOrSpawn(player, e.getKey(), e.getValue()));
    }
}
