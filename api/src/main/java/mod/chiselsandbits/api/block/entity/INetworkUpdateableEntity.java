package mod.chiselsandbits.api.block.entity;

import net.minecraft.nbt.CompoundTag;

/**
 * Marks the entity as a network updateable entity.
 */
public interface INetworkUpdateableEntity
{

    /**
     * Called when the entity is synced from NBT.
     *
     * @param tag The tag to load the synced data from.
     */
    void handleUpdateTag(CompoundTag tag);
}
