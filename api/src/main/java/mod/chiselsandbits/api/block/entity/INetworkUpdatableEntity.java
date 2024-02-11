package mod.chiselsandbits.api.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Marks the entity as a network updatable entity.
 */
public interface INetworkUpdatableEntity
{

    /**
     * {@return The position of the block entity.}
     */
    BlockPos getBlockPos();

    /**
     * Writes the current state of the entity to the buffer.
     * @param buffer The buffer to write to.
     */
    void serializeInto(FriendlyByteBuf buffer);

    /**
     * Reads the current state of the entity from the buffer.
     * @param buffer The buffer to read from.
     */
    void deserializeFrom(FriendlyByteBuf buffer);
}
