package mod.chiselsandbits.change.changes;

import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;

import java.util.Collection;

public class CombinedChange implements IChange
{
    private final Collection<IChange> changes;

    public CombinedChange(final Collection<IChange> changes) {this.changes = changes;}

    @Override
    public boolean canUndo()
    {
        return changes.stream().allMatch(IChange::canUndo);
    }

    @Override
    public boolean canRedo()
    {
        return changes.stream().allMatch(IChange::canRedo);
    }

    @Override
    public void undo() throws IllegalChangeAttempt
    {
        for (IChange change : changes)
        {
            change.undo();
        }
    }

    @Override
    public void redo() throws IllegalChangeAttempt
    {
        for (IChange change : changes)
        {
            change.redo();
        }
    }
}
