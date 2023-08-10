package mod.chiselsandbits.api.util;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Represents objects which can read and write to and from a packet buffer into or from a target object.
 * @param <D> The object to read from or write to.
 */
public interface IPacketBufferConverter<D>
{
    /**
     * Used to write the current instances data into a packet buffer.
     *
     * @param packetBuffer The packet buffer to write into.
     * @param source The source object to read from.
     */
    void serializeInto(@NotNull final FriendlyByteBuf packetBuffer, @NotNull final D source);

    /**
     * Used to read the data from the packet buffer into the current instance.
     * Potentially overriding the data that currently already exists in the instance.
     *
     * @param packetBuffer The packet buffer to read from.
     * @param target The target object to write to.
     */
    void deserializeFrom(@NotNull final FriendlyByteBuf packetBuffer, @NotNull final D target);
}
