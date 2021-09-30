package mod.chiselsandbits.api.inventory.bit;

import mod.chiselsandbits.api.item.bit.IBitItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

/**
 * Represents an inventory in which bits are contained.
 */
public interface IBitInventory
{
    /**
     * Checks if it is possible to extract exactly one bit with the given blockstate from the
     * the current inventory.
     *
     * @param blockState The blockstate.
     * @return {@code true} when extraction is possible.
     */
    default boolean canExtractOne(final BlockState blockState) {
        return this.canExtract(blockState, 1);
    }

    /**
     * Checks if it is possible to extract a given amount of bits with the given blockstate from the
     * the current inventory.
     *
     * @param blockState The blockstate.
     * @param count The amount of bits to extract.
     * @return {@code true} when extraction is possible.
     */
    boolean canExtract(final BlockState blockState, final int count);

    /**
     * Returns the maximal amount of bits with a given blockstate which can be extracted
     * of a given blockstate.
     * 
     * @param blockState The blockstate in question.
     * @return The amount of bits that can be extracted with a given blockstate.
     */
    int getMaxExtractAmount(final BlockState blockState);
    
    /**
     * Extracts exactly one bit with the given blockstate from the
     * the current inventory.
     *
     * @param blockState The blockstate.
     * @throws IllegalArgumentException when extraction is not possible.
     */
    default void extractOne(final BlockState blockState) throws IllegalArgumentException {
        this.extract(blockState, 1);
    }

    /**
     * Extracts a given amount of bits with the given blockstate from the
     * the current inventory.
     *
     * @param blockState The blockstate.
     * @param count The amount of bits to extract.
     * @throws IllegalArgumentException when extraction is not possible.
     */
    void extract(final BlockState blockState, final int count) throws IllegalArgumentException;

    /**
     * Checks if it is possible to insert exactly one bit with the given blockstate from the
     * the current inventory.
     *
     * @param blockState The blockstate.
     * @return {@code true} when insertion is possible.
     */
    default boolean canInsertOne(final BlockState blockState) {
        return this.canInsert(blockState, 1);
    }

    /**
     * Checks if it is possible to insert a given amount of bits with the given blockstate from the
     * the current inventory.
     *
     * @param blockState The blockstate.
     * @param count The amount of bits to insert.
     * @return {@code true} when insertion is possible.
     */
    boolean canInsert(final BlockState blockState, final int count);

    /**
     * Returns the maximal amount of bits with a given blockstate which can be inserted
     * of a given blockstate.
     *
     * @param blockState The blockstate in question.
     * @return The amount of bits that can be inserted with a given blockstate.
     */
    int getMaxInsertAmount(final BlockState blockState);

    /**
     * Inserts exactly one bit with the given blockstate from
     * the current inventory.
     *
     * @param blockState The blockstate.
     * @throws IllegalArgumentException when insertion is not possible.
     */
    default void insertOne(final BlockState blockState) throws IllegalArgumentException {
        this.insert(blockState, 1);
    }

    /**
     * Inserts a given amount of bits with the given blockstate from
     * the current inventory.
     *
     * @param blockState The blockstate.
     * @param count The amount of bits to insert.
     * @throws IllegalArgumentException when insertion is not possible.
     */
    void insert(final BlockState blockState, final int count) throws IllegalArgumentException;

    /**
     * Inserts a given amount of bits with the given blockstate from
     * the current inventory, discards bits that don't fit.
     *
     * @param blockState The blockstate.
     * @param count The amount of bits to insert.
     */
    default void insertOrDiscard(final BlockState blockState, final int count) {
        insert(blockState, Math.min(getMaxInsertAmount(blockState), count));
    }

    /**
     * Indicates if this inventory is empty or not.
     * @return {@code true} when empty.
     */
    boolean isEmpty();

    /**
     * Tries to insert a given itemstack with a bit item into the inventory.
     * Draining the itemstack completely if possible.
     *
     * @param stack The stack to insert.
     * @return The remainder, or the original stack if it is not an bit item.
     */
    default ItemStack insert(final ItemStack stack)
    {
        if (!(stack.getItem() instanceof IBitItem))
            return stack;

        final IBitItem bitItem = (IBitItem) stack.getItem();
        final BlockState blockState = bitItem.getBitState(stack);

        final int maxToInsertCount = this.getMaxInsertAmount(blockState);
        final int maxToInsertFromStack = Math.min(stack.getCount(), maxToInsertCount);
        final int maxRemainingOnStack = stack.getCount() - maxToInsertCount;

        this.insert(blockState, maxToInsertFromStack);

        final ItemStack resultStack = stack.copy();
        resultStack.setCount(maxRemainingOnStack);
        return resultStack;
    }
}
