package mod.chiselsandbits.api.chiseling.eligibility;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import org.jetbrains.annotations.NotNull;

/**
 * An object which can manage the eligibility of chiseling of blocks, blockstates,
 * or itemstacks (which contain blocks eligible) for chiseling.
 */
public interface IEligibilityManager
{

    static IEligibilityManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getEligibilityManager();
    }

    /**
     * Checks if a given blockstate can be chiseled or is already chiseled.
     *
     * @param state The given blockstate in question.
     *
     * @return True when chiselable or already chiseled.
     */
    default boolean canBeChiseled(@NotNull final BlockState state) {
        return analyse(state).canBeChiseled() || analyse(state).isAlreadyChiseled();
    }

    /**
     * Checks if a given block can be chiseled or is already chiseled.
     *
     * @param block The given block in question.
     *
     * @return True when chiselable or already chiseled.
     */
    default boolean canBeChiseled(@NotNull final Block block) {
        return analyse(block.defaultBlockState()).canBeChiseled();
    }

    /**
     * Checks if a given {@link IItemProvider} can be chiseled or is already chiseled.
     *
     * @param provider The given {@link IItemProvider} in question.
     *
     * @return True when chiselable or already chiseled.
     */
    default boolean canBeChiseled(@NotNull final IItemProvider provider)
    {
        return analyse(provider).canBeChiseled();
    }

    /**
     * Performs a chiselability analysis on the given blockstate.
     *
     * @param state The blockstate to analyze.
     *
     * @return The analysis result.
     */
    IEligibilityAnalysisResult analyse(@NotNull final BlockState state);

    /**
     * Performs a chiselability analysis on the given block.
     *
     * @param block The block to analyze.
     *
     * @return The analysis result.
     */
    default IEligibilityAnalysisResult analyse(@NotNull final Block block) {
        return analyse(block.defaultBlockState());
    }

    /**
     * Performs a chiselability analysis on the given {@link IItemProvider}.
     *
     * @param provider The {@link IItemProvider} to analyze.
     *
     * @return The analysis result.
     */
    IEligibilityAnalysisResult analyse(@NotNull final IItemProvider provider);
}
