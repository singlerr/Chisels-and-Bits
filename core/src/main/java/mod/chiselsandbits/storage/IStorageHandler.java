package mod.chiselsandbits.storage;

import mod.chiselsandbits.api.util.INBTSerializable;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import net.minecraft.nbt.CompoundTag;

/**
 * Represents a storage handler which can read and write data.
 * @param <P> The payload holder type that is used to pass data from an off-thread read into an on-thread read.
 */
public interface IStorageHandler<P> extends INBTSerializable<CompoundTag>, IPacketBufferSerializable
{
    /**
     * Invoked when the storage handler is being used to read NBT off-thread.
     * Needs to deserialize the nbt into a payload object which is then later on handed to {@link #syncPayloadOnGameThread(Object)} on the main game thread to prevent
     * having to sync the data back to the main thread.
     *
     * @param nbt The nbt to read.
     * @return The payload object.
     */
    P readPayloadOffThread(CompoundTag nbt);

    /**
     * Invoked when the storage handler is being used to read NBT off-thread, after it has been deserialized into a payload object
     * on the IO thread.
     *
     * @param payload The payload to write to target object.
     */
    void syncPayloadOnGameThread(P payload);
}
