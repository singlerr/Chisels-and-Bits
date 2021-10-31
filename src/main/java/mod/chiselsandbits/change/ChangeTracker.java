package mod.chiselsandbits.change;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.change.changes.BitChange;
import mod.chiselsandbits.change.changes.CombinedChange;
import mod.chiselsandbits.network.packets.ChangeTrackerUpdatedPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChangeTracker implements IChangeTracker
{
    protected final PlayerEntity player;
    protected final LinkedList<CombinedChange> changes = new LinkedList<>();
    protected int currentIndex = 0;

    public ChangeTracker()
    {
        this.player = null;
    }

    public ChangeTracker(final PlayerEntity player) {this.player = player;}

    public void reset() {
        changes.clear();
        sendUpdate();
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
              .map(e -> new BitChange(
                e.getKey(),
                e.getValue(),
                afterState.get(e.getKey())
              ))
              .collect(Collectors.toSet())
          )
        );

        currentIndex = 0;

        final int maxSize = IChiselsAndBitsAPI.getInstance().getConfiguration().getServer().changeTrackerSize.get();
        if (changes.size() > maxSize) {
            while(changes.size() > maxSize) {
                changes.removeLast();
            }
        }
        sendUpdate();
    }

    @Override
    public Deque<IChange> getChanges()
    {
        return new LinkedList<>(changes);
    }

    @Override
    public void clear()
    {
        changes.clear();
        sendUpdate();
    }

    public Optional<IChange> getCurrentUndo()
    {
        if (getChanges().size() <= currentIndex || currentIndex < 0) {
            return Optional.empty();
        }

        return Optional.of(changes.get(currentIndex));
    }

    public Optional<IChange> getCurrentRedo()
    {
        if (getChanges().size() < currentIndex || currentIndex < 1) {
            return Optional.empty();
        }

        return Optional.of(changes.get(currentIndex - 1));
    }

    @Override
    public boolean canUndo(final PlayerEntity player)
    {
        return getCurrentUndo().map(c -> c.canUndo(player)).orElse(false);
    }

    @Override
    public boolean canRedo(final PlayerEntity player)
    {
        return getCurrentRedo().map(c -> c.canRedo(player)).orElse(false);
    }

    @Override
    public void undo(final PlayerEntity player) throws IllegalChangeAttempt
    {
        if (!canUndo(player))
            throw new IllegalChangeAttempt();

        if (getCurrentUndo().isPresent()) {
            final IChange change = getCurrentUndo().get();
            change.undo(player);
            currentIndex = Math.min(changes.size(), currentIndex + 1);
            sendUpdate();
        }
    }

    @Override
    public void redo(final PlayerEntity player) throws IllegalChangeAttempt
    {
        if (!canRedo(player))
            throw new IllegalChangeAttempt();

        if (getCurrentRedo().isPresent()) {
            final IChange change = getCurrentRedo().get();
            change.redo(player);
            currentIndex = Math.max(0, currentIndex - 1);
            sendUpdate();
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.put("changes", this.changes.stream().map(INBTSerializable::serializeNBT).collect(Collectors.toCollection(ListNBT::new)));
        tag.putInt("index", this.currentIndex);
        return tag;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        this.changes.clear();
        this.changes.addAll(nbt.getList("changes", Constants.NBT.TAG_COMPOUND).stream().map(CombinedChange::new).collect(Collectors.toList()));
        this.currentIndex = nbt.getInt("index");
    }

    private void sendUpdate() {
        if (player != null && player instanceof ServerPlayerEntity)
        {
            ChiselsAndBits.getInstance().getNetworkChannel().sendToPlayer(
              new ChangeTrackerUpdatedPacket(this.serializeNBT()),
              (ServerPlayerEntity) player
            );
        }
    }
}
