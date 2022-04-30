package mod.chiselsandbits.storage;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

@Deprecated(since = "This is only related to legacy storage, and will be removed in a future version.", forRemoval = true)
public interface ILegacyStorageHandler extends IStorageHandler
{

    /**
     * Checks if the given tag is a legacy tag that this handler can handle.
     * The tag is not guaranteed to be valid for this handler, but that is what this method needs to determine,
     * as such it is not allowed to read or write to or from the given tag.
     *
     * @param compoundTag The compound tag in question.
     * @return {@code true} if the tag is a legacy tag that this handler can handle.
     */
    boolean matches(@NotNull final CompoundTag compoundTag);

    @Override
    default CompoundTag serializeNBT() {
        throw new UnsupportedOperationException("Legacy storage does not support serialization.");
    }
}
