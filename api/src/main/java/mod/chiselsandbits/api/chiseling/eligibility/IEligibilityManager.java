package mod.chiselsandbits.api.chiseling.eligibility;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
     * Checks if a given block information can be chiseled or is already chiseled.
     *
     * @param blockInformation The given block information in question.
     *
     * @return True when chiselable or already chiseled.
     */
    default boolean canBeChiseled(@NotNull final BlockInformation blockInformation) {
        return analyse(blockInformation).canBeChiseled() || analyse(blockInformation).isAlreadyChiseled();
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
     * Checks if a given {@link ItemLike} can be chiseled or is already chiseled.
     *
     * @param provider The given {@link ItemLike} in question.
     *
     * @return True when chiselable or already chiseled.
     */
    default boolean canBeChiseled(@NotNull final ItemLike provider)
    {
        return analyse(provider).canBeChiseled();
    }

    /**
     * Performs a chiselability analysis on the given block information.
     *
     * @param blockInformation The block information to analyze.
     *
     * @return The analysis result.
     */
    IEligibilityAnalysisResult analyse(@NotNull final BlockInformation blockInformation);

    /**
     * Performs a chiselability analysis on the given blockstate.
     *
     * @param state The blockstate to analyze.
     *
     * @return The analysis result.
     */
    default IEligibilityAnalysisResult analyse(@NotNull final BlockState state) {
        return analyse(new BlockInformation(state));
    }

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
     * Performs a chiselability analysis on the given {@link ItemLike}.
     *
     * @param provider The {@link ItemLike} to analyze.
     *
     * @return The analysis result.
     */
    IEligibilityAnalysisResult analyse(@NotNull final ItemLike provider);
}
