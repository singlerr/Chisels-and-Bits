package mod.chiselsandbits.storage;

import com.google.common.collect.ImmutableList;
import mod.chiselsandbits.platforms.core.util.constants.NbtConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Deprecated(since = "This is only related to legacy storage, and will be removed in a future version.", forRemoval = true)
final class LegacyAwareStorageEngine implements IStorageEngine
{

    private final LegacyVersionedStorageEngine legacyVersionedStorageEngine;
    private final VersionedStorageEngine versionedStorageEngine;

    private final Collection<IStorageHandler> handlers;

    LegacyAwareStorageEngine(final LegacyVersionedStorageEngine legacyVersionedStorageEngine, final VersionedStorageEngine versionedStorageEngine) {
        this.legacyVersionedStorageEngine = legacyVersionedStorageEngine;
        this.versionedStorageEngine = versionedStorageEngine;

        handlers = ImmutableList.<IStorageHandler>builder()
                     .addAll(legacyVersionedStorageEngine.getHandlers())
                     .addAll(versionedStorageEngine.getHandlers())
                     .build();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return versionedStorageEngine.serializeNBT();
    }

    @Override
    public void serializeNBTInto(final CompoundTag tag)
    {
        versionedStorageEngine.serializeNBTInto(tag);
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        if (nbt.contains(NbtConstants.VERSION)) {
            //This is considered a versioned implementation.
            versionedStorageEngine.deserializeNBT(nbt);
            return;
        }

        legacyVersionedStorageEngine.deserializeNBT(nbt);
    }

    @Override
    public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
    {
        versionedStorageEngine.serializeInto(packetBuffer);
    }

    @Override
    public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
    {
        versionedStorageEngine.deserializeFrom(packetBuffer);
    }

    @Override
    public Collection<? extends IStorageHandler> getHandlers()
    {
        return handlers;
    }
}
