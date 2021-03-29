package mod.chiselsandbits.api.block.entity;

import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;

import java.util.Map;

/**
 * The statistics of a multistate block.
 */
public interface IMultiStateBlockStatistics
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
     * Indicates the slipperiness of the current block.
     *
     * @return The slipperiness.
     */
    float getSlipperiness();
}
