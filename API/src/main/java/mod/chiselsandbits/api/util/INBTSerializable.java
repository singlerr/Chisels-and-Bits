package mod.chiselsandbits.api.util;

import net.minecraft.nbt.Tag;

/**
 * Defines objects which can be persisted in NBT.
 * @param <T> The type of the NBT that is used to persist the object.
 */
public interface INBTSerializable<T extends Tag>
{
    /**
     * Invoked to serialize the object to nbt.
     * @return The NTB persistent data of the object.
     */
    T serializeNBT();

    /**
     * Invoked to override the current objects' data with the data stored in the persistent NBT.
     * @param nbt The data to load our values from.
     */
    void deserializeNBT(T nbt);
}
