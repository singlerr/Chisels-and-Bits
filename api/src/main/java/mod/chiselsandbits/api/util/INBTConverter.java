package mod.chiselsandbits.api.util;

import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * Defines converts which can persist data in NBT.
 * @param <T> The type of the NBT that is used to persist the object.
 */
public interface INBTConverter<T extends Tag> extends INBTSerializable<T>
{

    /**
     * Writes the object to NBT.
     * Without the object having to create the tag itself.
     *
     * @param tag The tag to save into.
     */
    void serializeNBTInto(final T tag);
}
