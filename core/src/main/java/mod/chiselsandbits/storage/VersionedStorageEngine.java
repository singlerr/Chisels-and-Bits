package mod.chiselsandbits.storage;

import com.mojang.logging.LogUtils;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

final class VersionedStorageEngine implements IStorageEngine
{
    private static final Logger LOGGER = LogUtils.getLogger();

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
        if (nbt.isEmpty())
        {
            LOGGER.warn("Empty NBT tag received, ignoring.");
            return;
        }

        if (!nbt.contains(NbtConstants.VERSION))
            LOGGER.warn("The given NBT did not contain a versioned storage data entry. Missing the version!");

        final int version = nbt.contains(NbtConstants.VERSION) ? nbt.getInt(NbtConstants.VERSION) : minimalVersion;
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
        deserializeNBT(Objects.requireNonNull(packetBuffer.readAnySizeNbt()));
    }

    @Override
    public Collection<IStorageHandler> getHandlers()
    {
        return handlers;
    }
}
