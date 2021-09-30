package mod.chiselsandbits.change;

import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.change.ICombiningChangeTracker;
import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.change.changes.BlockBrokenChange;
import mod.chiselsandbits.change.changes.BlockPlacedChange;
import mod.chiselsandbits.change.changes.BlockUpdatedChange;
import mod.chiselsandbits.change.changes.CombinedChange;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class ChangeTracker implements IChangeTracker
{

    protected final Deque<IChange> changes = new LinkedList<>();
    protected final Player         player;

    public ChangeTracker(final Player player) {this.player = player;}

    public void reset() {
        changes.clear();
    }

    @Override
    public void onBlocksBroken(final Map<BlockPos, IMultiStateSnapshot> snapshots)
    {
        changes.addFirst(
          new CombinedChange(
            snapshots.entrySet().stream()
              .map(e -> new BlockBrokenChange(
                player.level,
                player,
                e.getKey(),
                e.getValue(),
                false
              ))
              .collect(Collectors.toSet())
          )
        );
    }

    @Override
    public void onLastBitsRemoved(final Map<BlockPos, IMultiStateSnapshot> snapshots)
    {
        changes.addFirst(
          new CombinedChange(
            snapshots.entrySet().stream()
              .map(e -> new BlockBrokenChange(
                player.level,
                player,
                e.getKey(),
                e.getValue(),
                true
              ))
              .collect(Collectors.toSet())
          )
        );
    }

    @Override
    public void onBlocksPlaced(
      final Map<BlockPos, BlockState> initialStates, final Map<BlockPos, IMultiStateSnapshot> targetStates)
    {
        if (!initialStates.keySet().containsAll(targetStates.keySet()) || !targetStates.keySet().containsAll(initialStates.keySet()))
            throw new IllegalArgumentException("Initial States and Target States reference difference block positions");

        changes.addFirst(
          new CombinedChange(
            initialStates.entrySet().stream()
              .map(e -> new BlockPlacedChange(
                player.level,
                player,
                e.getKey(),
                e.getValue(),
                targetStates.get(e.getKey()),
                false
              ))
              .collect(Collectors.toSet())
          )
        );
    }

    @Override
    public void onFirstBitsPlaced(
      final Map<BlockPos, BlockState> initialStates, final Map<BlockPos, IMultiStateSnapshot> targetStates)
    {
        if (!initialStates.keySet().containsAll(targetStates.keySet()) || !targetStates.keySet().containsAll(initialStates.keySet()))
            throw new IllegalArgumentException("Initial States and Target States reference difference block positions");

        changes.addFirst(
          new CombinedChange(
            initialStates.entrySet().stream()
              .map(e -> new BlockPlacedChange(
                player.level,
                player,
                e.getKey(),
                e.getValue(),
                targetStates.get(e.getKey()),
                true
              ))
              .collect(Collectors.toSet())
          )
        );
    }

    @Override
    public void onBlocksUpdated(
      final Map<BlockPos, IMultiStateSnapshot> beforeStates, final Map<BlockPos, IMultiStateSnapshot> afterState)
    {
        if (!beforeStates.keySet().containsAll(afterState.keySet()) || !afterState.keySet().containsAll(beforeStates.keySet()))
            throw new IllegalArgumentException("Initial States and Target States reference difference block positions");

        changes.addFirst(
          new CombinedChange(
            beforeStates.entrySet().stream()
              .map(e -> new BlockUpdatedChange(
                player.level,
                player,
                e.getKey(),
                e.getValue(),
                afterState.get(e.getKey())
              ))
              .collect(Collectors.toSet())
          )
        );
    }

    @Override
    public ICombiningChangeTracker openToCombine()
    {
        return new CombiningChangeTracker(player, changes::add);
    }

    @Override
    public Deque<IChange> getChanges()
    {
        return new LinkedList<>(changes);
    }
}
