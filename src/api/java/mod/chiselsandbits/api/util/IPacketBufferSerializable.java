package mod.chiselsandbits.api.util;

import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents objects which can be read and written to and from a packet buffer.
 */
public interface IPacketBufferSerializable
{
    /**
     * Used to write the current instances data into a packet buffer.
     *
     * @param packetBuffer The packet buffer to write into.
     */
    void serializeInto(@NotNull final PacketBuffer packetBuffer);

    /**
     * Used to read the data from the packet buffer into the current instance.
     * Potentially overriding the data that currently already exists in the instance.
     *
     * @param packetBuffer The packet buffer to read from.
     */
    void deserializeFrom(@NotNull final PacketBuffer packetBuffer);
}
