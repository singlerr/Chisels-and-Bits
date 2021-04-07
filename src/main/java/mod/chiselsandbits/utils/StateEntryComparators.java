package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;

import java.util.Comparator;

public class StateEntryComparators
{

    private StateEntryComparators()
    {
        throw new IllegalStateException("Can not instantiate an instance of: StateEntryComparators. This is a utility class");
    }

    public static Comparator<IStateEntryInfo> xzySorted() {
        return Comparator.<IStateEntryInfo, Double>comparing(i -> i.getStartPoint().getX())
                 .thenComparing(i -> i.getStartPoint().getY())
                 .thenComparing(i -> i.getStartPoint().getZ());
    }

    public static Comparator<IStateEntryInfo> yzxSorted() {
        return Comparator.<IStateEntryInfo, Double>comparing(i -> i.getStartPoint().getY())
                 .thenComparing(i -> i.getStartPoint().getZ())
                 .thenComparing(i -> i.getStartPoint().getX());
    }

    public static Comparator<IStateEntryInfo> zyxSorted() {
        return Comparator.<IStateEntryInfo, Double>comparing(i -> i.getStartPoint().getZ())
                 .thenComparing(i -> i.getStartPoint().getY())
                 .thenComparing(i -> i.getStartPoint().getX());
    }
}
