package mod.chiselsandbits.change.changes;

import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
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

public class BlockBrokenChange implements IChange
{

    private final Level    world;
    private final Player              player;
    private final BlockPos            blockPos;
    private final IMultiStateSnapshot brokenSnapshot;
    private final boolean bitBased;

    public BlockBrokenChange(final Level world, final Player player, final BlockPos blockPos, final IMultiStateSnapshot brokenSnapshot, final boolean bitBased) {
        this.world = world;
        this.player = player;
        this.blockPos = blockPos;
        this.brokenSnapshot = brokenSnapshot;
        this.bitBased = bitBased;
    }

    @Override
    public boolean canUndo()
    {
        final BlockState primaryState = this.brokenSnapshot.getStatics().getPrimaryState();

        if (!hasSnapshotStackOrBits())
            return false;

        return world.getBlockState(blockPos).isAir() && IConversionManager.getInstance().getChiseledVariantOf(primaryState).isPresent();
    }

    @Override
    public boolean canRedo()
    {
        final BlockEntity tileEntity = world.getBlockEntity(blockPos);
        if (!(tileEntity instanceof IMultiStateBlockEntity))
            return false;

        final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
        return brokenSnapshot.createNewShapeIdentifier().equals(multiStateBlockEntity.createNewShapeIdentifier());
    }

    @Override
    public void undo() throws IllegalChangeAttempt
    {
        if (!canUndo())
            throw new IllegalChangeAttempt();

        final BlockState primaryState = this.brokenSnapshot.getStatics().getPrimaryState();
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
            this.brokenSnapshot.stream().forEach(
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
    public void redo() throws IllegalChangeAttempt
    {
        if (!canRedo())
            throw new IllegalChangeAttempt();

        world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
        insertSnapshotStackOrBits();
    }

    private boolean hasSnapshotStackOrBits() {
        if (!bitBased)
            return InventoryUtils.getChiseledStackMatchingSnapshot(player, brokenSnapshot).isEmpty();

        final IBitInventory playerInventory = IBitInventoryManager.getInstance().create(player);
        return  brokenSnapshot.getStatics().getStateCounts().entrySet().stream()
                  .filter(e -> !e.getKey().isAir())
                  .allMatch(e -> playerInventory.canExtract(e.getKey(), e.getValue()));
    }

    private void extractShapshotStackOrBits() {
        if (!bitBased)
        {
            InventoryUtils.extractChiseledStackMatchingSnapshot(player, brokenSnapshot);
            return;
        }

        final IBitInventory playerInventory = IBitInventoryManager.getInstance().create(player);
        brokenSnapshot.getStatics().getStateCounts().entrySet().stream()
          .filter(e -> !e.getKey().isAir())
          .forEach(e -> playerInventory.extract(e.getKey(), e.getValue()));
    }

    private void insertSnapshotStackOrBits() {
        if (!bitBased) {
            player.getInventory().add(brokenSnapshot.toItemStack().toBlockStack());
            return;
        }

        brokenSnapshot.getStatics().getStateCounts().entrySet().stream()
          .filter(e -> !e.getKey().isAir())
          .forEach(e -> BitInventoryUtils.insertIntoOrSpawn(player, e.getKey(), e.getValue()));
    }
}
