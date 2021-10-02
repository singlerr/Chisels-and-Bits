package mod.chiselsandbits.change;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.change.changes.BlockUpdatedChange;
import mod.chiselsandbits.change.changes.CombinedChange;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChangeTracker implements IChangeTracker
{
    protected final LinkedList<IChange> changes = new LinkedList<>();
    protected final ThreadLocal<Integer> currentIndex = new ThreadLocal<>();

    public ChangeTracker() {
        this.currentIndex.set(0);
    }

    public void reset() {
        changes.clear();
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
                e.getKey(),
                e.getValue(),
                afterState.get(e.getKey())
              ))
              .collect(Collectors.toSet())
          )
        );

        final int maxSize = IChiselsAndBitsAPI.getInstance().getConfiguration().getServer().changeTrackerSize.get();
        if (changes.size() > maxSize) {
            while(changes.size() > maxSize) {
                changes.removeLast();
            }
        }
    }

    @Override
    public Deque<IChange> getChanges()
    {
        return new LinkedList<>(changes);
    }

    public Optional<IChange> getCurrentUndo()
    {
        if (getChanges().size() <= currentIndex.get() || currentIndex.get() < 0) {
            return Optional.empty();
        }

        return Optional.of(changes.get(currentIndex.get()));
    }

    public Optional<IChange> getCurrentRedo()
    {
        if (getChanges().size() <= currentIndex.get() || currentIndex.get() < 1) {
            return Optional.empty();
        }

        return Optional.of(changes.get(currentIndex.get() - 1));
    }

    @Override
    public boolean canUndo(final Player player)
    {
        return getCurrentUndo().map(c -> c.canUndo(player)).orElse(false);
    }

    @Override
    public boolean canRedo(final Player player)
    {
        return getCurrentRedo().map(c -> c.canRedo(player)).orElse(false);
    }

    @Override
    public void undo(final Player player) throws IllegalChangeAttempt
    {
        if (!canUndo(player))
            throw new IllegalChangeAttempt();

        if (getCurrentUndo().isPresent()) {
            final IChange change = getCurrentUndo().get();
            change.undo(player);
            currentIndex.set(Math.max(changes.size(), currentIndex.get() + 1));
        }
    }

    @Override
    public void redo(final Player player) throws IllegalChangeAttempt
    {
        if (!canRedo(player))
            throw new IllegalChangeAttempt();

        if (getCurrentRedo().isPresent()) {
            final IChange change = getCurrentRedo().get();
            change.redo(player);
            currentIndex.set(Math.max(0, currentIndex.get() - 1));
        }
    }
}
