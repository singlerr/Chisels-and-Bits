package mod.chiselsandbits.api.item.click;

/**
 * Represents the continuous processing state of a click interaction.
 */
public class ClickProcessingState
{

    public static final ClickProcessingState ALLOW = new ClickProcessingState(true, ProcessingResult.ALLOW);
    public static final ClickProcessingState DENIED = new ClickProcessingState(true, ProcessingResult.DENY);
    public static final ClickProcessingState DEFAULT = new ClickProcessingState(false, ProcessingResult.DEFAULT);

    private final boolean      shouldCancel;
    private final ProcessingResult nextState;

    public ClickProcessingState(final boolean shouldCancel, final ProcessingResult nextState)
    {
        this.shouldCancel = shouldCancel;
        this.nextState = nextState;
    }

    public boolean shouldCancel()
    {
        return shouldCancel;
    }

    public ProcessingResult getNextState()
    {
        return nextState;
    }

    public enum ProcessingResult
    {
        DENY,
        DEFAULT,
        ALLOW
    }
}
