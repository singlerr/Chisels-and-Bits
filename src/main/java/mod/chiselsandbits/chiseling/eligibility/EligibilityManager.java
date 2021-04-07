package mod.chiselsandbits.chiseling.eligibility;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityAnalysisResult;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import net.minecraft.block.Block;
import net.minecraft.util.IItemProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class EligibilityManager implements IEligibilityManager
{
    private static final EligibilityManager INSTANCE = new EligibilityManager();

    private static final Cache<Block, IEligibilityAnalysisResult> cache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build();

    public static EligibilityManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Performs a chiselability analysis on the given block.
     *
     * @param block The block to analyze.
     * @return The analysis result.
     */
    @Override
    public IEligibilityAnalysisResult analyse(@NotNull final Block block)
    {
        return cache.;
    }

    /**
     * Performs a chiselability analysis on the given {@link IItemProvider}.
     *
     * @param provider The {@link IItemProvider} to analyze.
     * @return The analysis result.
     */
    @Override
    public IEligibilityAnalysisResult analyse(@NotNull final IItemProvider provider)
    {
        return null;
    }

    private EligibilityManager()
    {
    }
}
