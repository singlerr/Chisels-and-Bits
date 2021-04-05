package mod.chiselsandbits.api.item.withmode;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an item with several different operational modes.
 *
 * @param <M> The enum specifying the mode.
 */
public interface IWithModeItem<M>
{

    /**
     * Returns the current mode of the itemstack.
     * @param stack The stack in question.
     * @return The mode the given stack has.
     */
    @NotNull
    M getMode(final ItemStack stack);

    /**
     * Set the given mode on the given itemstack.
     *
     * @param stack The stack to set the mode on.
     * @param mode The mode to set on the stack.
     */
    void setMode(final ItemStack stack, final M mode);

    /**
     * Returns all possible modes this item can have.
     *
     * @return The possible modes in an array.
     */
    @NotNull
    Iterable<M> getPossibleModes();
}
