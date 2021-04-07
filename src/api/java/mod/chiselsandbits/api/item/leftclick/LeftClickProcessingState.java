package mod.chiselsandbits.api.item.leftclick;

import net.minecraftforge.eventbus.api.Event;

/**
 * Represents the continuous processing state of a left click interaction.
 */
public class LeftClickProcessingState
{

    public static final LeftClickProcessingState DEFAULT = new LeftClickProcessingState(false, Event.Result.DEFAULT);

    private final boolean      shouldCancel;
    private final Event.Result nextState;

    public LeftClickProcessingState(final boolean shouldCancel, final Event.Result nextState)
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
