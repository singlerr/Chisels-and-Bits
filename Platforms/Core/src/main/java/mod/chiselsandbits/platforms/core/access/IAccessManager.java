package mod.chiselsandbits.platforms.core.access;

import mod.chiselsandbits.platforms.core.IChiselsAndBitsPlatformCore;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * Provides platform specific access management to base game object values which are normally not
 * accessible.
 */
public interface IAccessManager
{

    /**
     * The access manager for this platform.
     *
     * @return The access manager.
     */
    static IAccessManager getInstance() {
        return IChiselsAndBitsPlatformCore.getInstance().getAccessManager();
    }

    /**
     * Gives access to the storage of the given chunk section.
     * This field is normally private.
     *
     * @param levelChunkSection The chunk section to get the storage from.
     * @return The storage of the chunk section.
     */
    BitStorage getStorageFrom(final LevelChunkSection levelChunkSection);
}
