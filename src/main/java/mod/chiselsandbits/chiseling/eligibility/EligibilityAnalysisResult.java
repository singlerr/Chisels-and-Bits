package mod.chiselsandbits.chiseling.eligibility;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityAnalysisResult;
import net.minecraft.util.text.IFormattableTextComponent;

public class EligibilityAnalysisResult implements IEligibilityAnalysisResult
{

    private final boolean canBeChiseled;
    private final boolean isAlreadyChiseled;
    private final IFormattableTextComponent displayName;

    public EligibilityAnalysisResult(final boolean canBeChiseled, final boolean isAlreadyChiseled, final IFormattableTextComponent displayName) {
        this.canBeChiseled = canBeChiseled;
        this.isAlreadyChiseled = isAlreadyChiseled;
        this.displayName = displayName;
    }

    /**
     * Indicates if the requested object can be chiseled.
     *
     * @return True for chiselability.
     */
    @Override
    public boolean canBeChiseled()
    {
        return canBeChiseled;
    }

    /**
     * Indicates if the requested object is already chiseled.
     *
     * @return True when already chiseled.
     */
    @Override
    public boolean isAlreadyChiseled()
    {
        return isAlreadyChiseled;
    }

    /**
     * The reason why a given eligibility result returned if it can be chiseled or not.
     *
     * @return The displayable reason.
     */
    @Override
    public IFormattableTextComponent getReason()
    {
        return displayName;
    }
}
