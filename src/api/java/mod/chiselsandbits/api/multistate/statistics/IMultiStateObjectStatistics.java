package mod.chiselsandbits.api.multistate.statistics;

import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;

/**
 * The statistics of a multistate block.
 */
public interface IMultiStateObjectStatistics
{

    /**
     * The primary blockstate.
     * If the multistate block is empty, then {@link AirBlock#getDefaultState()} is returned.
     * Generally speaking this is the blockstate that occurs the most often
     * in the multistate block.
     * But this is not guaranteed.
     *
     * @return The primary block state of the multi state block.
     */
    BlockState getPrimaryState();

    /**
     * An immutable copy of the map that contains the counts of all blockstates in the multistate block.
     * @return The blockstate count map.
     */
    Map<BlockState, Integer> getStateCounts();

    /**
     * Indicates if the block that this statistics belongs to should check for weak power.
     *
     * @return Indicates if this block should check for weak power.
     */
    boolean shouldCheckWeakPower();

    /**
     * Gives access to the fullness factor of the current block.
     *
     * @return The fullness factor.
     */
    float getFullnessFactor();

    /**
     * Indicates if the current block is full.
     * Generally coincides with {@link #getFullnessFactor()} being equal to {@code 1f}.
     *
     * @return Indicates if this block is full or not.
     */
    default boolean isFullBlock() {
        return getFullnessFactor() == 1f;
    }

    /**
     * Indicates the slipperiness of the current block.
     *
     * @return The slipperiness.
     */
    float getSlipperiness();

    /**
     * The factor of how much light is emitted by the block.
     *
     * @return A factor of how much light is emitted by the block, between 0 and 1.
     */
    float getLightEmissionFactor();

    /**
     * Indicates the relative block hardness for a given player.
     *
     * @param player The players to get the hardness for.
     *
     * @return The relative block hardness.
     */
    float getRelativeBlockHardness(final PlayerEntity player);

    /**
     * Indicates if the current multistate block is empty.
     *
     * @return The current multistate block.
     */
    default boolean isEmptyBlock() {
        return getFullnessFactor() == 0f;
    }

    /**
     * Indicates if the current multistate block can propagate skylight.
     *
     * @return True when the block can propagate skylight, false when not.
     */
    boolean canPropagateSkylight();
}
