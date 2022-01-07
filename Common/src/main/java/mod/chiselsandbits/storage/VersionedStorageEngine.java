package mod.chiselsandbits.storage;

import mod.chiselsandbits.platforms.core.util.constants.NbtConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

final class VersionedStorageEngine implements IStorageEngine
{
    private final int                      minimalVersion;
    private final List<IStorageHandler> handlers;

    private final int currentVersion;
    private final IStorageHandler saveHandler;

    VersionedStorageEngine(final LinkedList<IStorageHandler> handlers) {
        this(0, handlers);
    }

    VersionedStorageEngine(final int minimalVersion, final LinkedList<IStorageHandler> handlers) {
        Validate.notEmpty(handlers);

        this.minimalVersion = minimalVersion;
        this.handlers = Collections.unmodifiableList(handlers);

        this.saveHandler = handlers.getLast();
        this.currentVersion = minimalVersion + handlers.size() - 1;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundTag = new CompoundTag();

        serializeNBTInto(compoundTag);

        return compoundTag;
    }

    @Override
    public void serializeNBTInto(final CompoundTag tag)
    {
        tag.putInt(NbtConstants.VERSION, currentVersion);
        tag.put(NbtConstants.DATA, this.saveHandler.serializeNBT());
    }

    @Override
    public void deserializeNBT(final @NotNull CompoundTag nbt)
    {
        if (!nbt.contains(NbtConstants.VERSION))
            throw new IllegalArgumentException("The given NBT did not contain a versioned storage data entry. Missing the version!");

        final int version = nbt.getInt(NbtConstants.VERSION);
        if (version < minimalVersion)
            throw new IllegalArgumentException("The given NBT did contained a version storage data entry, which is of an unsupported version. The version is " + version + ", but the minimal version is " + minimalVersion);

        if (version > currentVersion)
            throw new IllegalArgumentException("The given NBT did contained a version storage data entry, which is of an unsupported version. The version is " + version + ", but the current version is " + currentVersion);

        final int index = version - minimalVersion;
        final IStorageHandler handler = handlers.get(index);
        handler.deserializeNBT(nbt.getCompound(NbtConstants.DATA));
    }

    @Override
    public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeNbt(serializeNBT());
    }

    @Override
    public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
    {
        deserializeNBT(packetBuffer.readAnySizeNbt());
    }

    @Override
    public Collection<IStorageHandler> getHandlers()
    {
        return handlers;
    }
}
