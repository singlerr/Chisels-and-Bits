package mod.chiselsandbits.api.multistate.accessor;

import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;

import java.util.stream.Stream;

/**
 * Gives access to all states in a given area.
 * Might be larger then a single block.
 */
public interface IAreaAccessor
{
    /**
     * Gives access to a stream with the entry state info inside the accessors range.
     *
     * @return The stream with the inner states.
     */
    Stream<IStateEntryInfo> stream();

    /**
     * Creates a snapshot of the current state.
     *
     * @return The snapshot.
     */
    IMultiStateSnapshot createSnapshot();
}
