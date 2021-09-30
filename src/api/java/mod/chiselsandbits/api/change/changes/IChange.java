package mod.chiselsandbits.api.change.changes;

/**
 * Represents a single change that has been created with bits.
 */
public interface IChange
{

    /**
     * Checks if the change can still be undone.
     * @return True when the change can be undone.
     */
    boolean canUndo();

    /**
     * Checks if the change can still be redone.
     * @return True when the change can be redone.
     */
    boolean canRedo();

    /**
     * Undoes the change.
     */
    void undo() throws IllegalChangeAttempt;

    /**
     * Redoes the change
     */
    void redo() throws IllegalChangeAttempt;
}
