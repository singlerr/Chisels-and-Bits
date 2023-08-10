package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.multistate.snapshot.LazilyDecodingSingleBlockMultiStateSnapshot;

public class MultiStateSnapshotUtils
{

    private MultiStateSnapshotUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: MultiStateSnapshotUtils. This is a utility class");
    }

    public static IMultiStateSnapshot createFromStorage(final IStateEntryStorage storage) {
        return new LazilyDecodingSingleBlockMultiStateSnapshot(storage.serializeNBT());
    }
}
