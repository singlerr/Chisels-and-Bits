package mod.chiselsandbits.api.change.changes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Represents a single change that has been created with bits.
 */
public interface IChange extends INBTSerializable<CompoundTag>
{
    /**
     * Checks if the change can still be undone.
     * @return True when the change can be undone.
     */
    boolean canUndo(final Player player);

    /**
     * Checks if the change can still be redone.
     * @return True when the change can be redone.
     */
    boolean canRedo(final Player player);

    /**
     * Undoes the change.
     */
    void undo(final Player player) throws IllegalChangeAttempt;

    /**
     * Redoes the change
     */
    void redo(final Player player) throws IllegalChangeAttempt;
}
