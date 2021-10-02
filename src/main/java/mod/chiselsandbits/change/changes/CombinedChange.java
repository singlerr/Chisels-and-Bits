package mod.chiselsandbits.change.changes;

import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class CombinedChange implements IChange
{
    private final Collection<IChange> changes;

    public CombinedChange(final Collection<IChange> changes) {this.changes = changes;}

    @Override
    public boolean canUndo(final Player player)
    {
        for (IChange change : changes)
        {
            if (!change.canUndo(player))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canRedo(final Player player)
    {
        for (IChange change : changes)
        {
            if (!change.canRedo(player))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void undo(final Player player) throws IllegalChangeAttempt
    {
        for (IChange change : changes)
        {
            change.undo(player);
        }
    }

    @Override
    public void redo(final Player player) throws IllegalChangeAttempt
    {
        for (IChange change : changes)
        {
            change.redo(player);
        }
    }
}
