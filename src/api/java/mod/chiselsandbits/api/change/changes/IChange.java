package mod.chiselsandbits.api.change.changes;

import net.minecraft.entity.player.PlayerEntity;

/**
 * Represents a single change that has been created with bits.
 */
public interface IChange
{
    /**
     * Checks if the change can still be undone.
     * @return True when the change can be undone.
     */
    boolean canUndo(final PlayerEntity player);

    /**
     * Checks if the change can still be redone.
     * @return True when the change can be redone.
     */
    boolean canRedo(final PlayerEntity player);

    /**
     * Undoes the change.
     */
    void undo(final PlayerEntity player) throws IllegalChangeAttempt;

    /**
     * Redoes the change
     */
    void redo(final PlayerEntity player) throws IllegalChangeAttempt;
}
