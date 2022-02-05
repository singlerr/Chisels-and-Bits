package mod.chiselsandbits.api.block.storage;

import mod.chiselsandbits.api.multistate.mutator.IMirrorAndRotateble;
import mod.chiselsandbits.api.util.INBTSerializable;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.function.BiConsumer;

public interface IStateEntryStorage extends IPacketBufferSerializable, IMirrorAndRotateble, INBTSerializable<CompoundTag>
{
    /**
     * The size in all dimensions of the storage.
     *
     * @return The size in all dimensions of the storage.
     */
    int getSize();

    /**
     * Clears the current storage and sets its internal data to the {@link Blocks#AIR} block on all positions.
     */
    void clear();

    /**
     * Initializes the storage on all positions with the given blockstate, removing all other states from the storage.
     *
     * @param currentState The new blockstate to set on all positions.
     */
    void initializeWith(BlockState currentState);

    /**
     * Loads the storage by cloning the chunk section in to the storage.
     *
     * @param chunkSection The chunk section to clone.
     */
    void loadFromChunkSection(LevelChunkSection chunkSection);

    /**
     * Gets the blockstate in the storage on the given position.
     *
     * @param x The x offset, from to {@link #getSize()} excluding.
     * @param y The y offset, from to {@link #getSize()} excluding.
     * @param z The z offset, from to {@link #getSize()} excluding.
     * @return The blockstate on the given position.
     */
    BlockState getBlockState(int x, int y, int z);

    /**
     * Gets the blockstate in the storage on the given position.
     *
     * @param  coordinate The coordinate to get the blockstate from.
     * @return The blockstate on the given position.
     */
    default BlockState getBlockState(Vec3i coordinate) {
        return getBlockState(coordinate.getX(), coordinate.getY(), coordinate.getZ());
    }

    /**
     * Sets the blockstate in the storage on the given position.
     *
     * @param x The x offset, from to {@link #getSize()} excluding.
     * @param y The y offset, from to {@link #getSize()} excluding.
     * @param z The z offset, from to {@link #getSize()} excluding.
     * @param blockState The blockstate to set.
     */
    void setBlockState(int x, int y, int z, BlockState blockState);

    /**
     * Counts the entries of this storage and passes the results into the consumer.
     *
     * @param storageConsumer The consumer of the results.
     */
    void count(BiConsumer<BlockState, Integer> storageConsumer);

    /**
     * The internal raw data array.
     *
     * @return The raw data array.
     */
    byte[] getRawData();

    /**
     * Creates a copy of this storage.
     *
     * @return A copy of this storage.
     */
    IStateEntryStorage createSnapshot();

    /**
     * Clears the storage and fills it from the bottom with the blockstate for the given amount of entries.
     *
     * @param state The state to fill with.
     * @param entries The entries to fill with.
     */
    void fillFromBottom(BlockState state, int entries);
}
