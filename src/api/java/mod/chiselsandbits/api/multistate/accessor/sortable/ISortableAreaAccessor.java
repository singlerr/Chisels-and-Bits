package mod.chiselsandbits.api.multistate.accessor.sortable;

import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;

import java.util.stream.Stream;

/**
 * A special area accessor which enabled a stream option
 * that can be passed a position mutator.
 */
public interface ISortableAreaAccessor extends IAreaAccessor
{


    Stream<IStateEntryInfo> streamWithPositionMutator(final IPositionMutator positionMutator);
}
