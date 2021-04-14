package mod.chiselsandbits.api.inventory.bit;

import net.minecraft.block.BlockState;

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
     * Inserts exactly one bit with the given blockstate from the
     * the current inventory.
     *
     * @param blockState The blockstate.
     * @throws IllegalArgumentException when insertion is not possible.
     */
    default void insertOne(final BlockState blockState) throws IllegalArgumentException {
        this.insert(blockState, 1);
    }

    /**
     * Inserts a given amount of bits with the given blockstate from the
     * the current inventory.
     *
     * @param blockState The blockstate.
     * @param count The amount of bits to insert.
     * @throws IllegalArgumentException when insertion is not possible.
     */
    void insert(final BlockState blockState, final int count) throws IllegalArgumentException;
}
