package mod.chiselsandbits.storage;

import mod.chiselsandbits.api.util.INBTConverter;
import mod.chiselsandbits.api.util.INBTSerializable;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import net.minecraft.nbt.CompoundTag;

import java.util.Collection;

/**
 * Represents a storage engine, which can process data for IO purposes.
 */
public interface IStorageEngine extends INBTConverter<CompoundTag>, IPacketBufferSerializable
{

    /**
     * Gives access to all handlers which are known to the engine.
     * This collection is unmodifiable.
     * This collection should not be used to modify storage data.
     *
     * Use the relevant supported options to modify storage data.
     *
     * @return The collection of supported handlers
     */
    Collection<? extends IStorageHandler<?>> getHandlers();


}
