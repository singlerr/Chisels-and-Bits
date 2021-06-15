package mod.chiselsandbits.api.item.click;

import net.minecraftforge.eventbus.api.Event;

/**
 * Represents the continuous processing state of a click interaction.
 */
public class ClickProcessingState
{

    public static final ClickProcessingState DEFAULT = new ClickProcessingState(false, Event.Result.DEFAULT);

    private final boolean      shouldCancel;
    private final Event.Result nextState;

    public ClickProcessingState(final boolean shouldCancel, final Event.Result nextState)
    {
        this.shouldCancel = shouldCancel;
        this.nextState = nextState;
    }

    public boolean shouldCancel()
    {
        return shouldCancel;
    }

    public Event.Result getNextState()
    {
        return nextState;
    }
}
