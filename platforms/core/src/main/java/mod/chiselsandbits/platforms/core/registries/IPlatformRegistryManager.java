package mod.chiselsandbits.platforms.core.registries;

import mod.chiselsandbits.platforms.core.IChiselsAndBitsPlatformCore;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistrarManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/**
 * Gives access to the platforms registries at runtime.
 */
public interface IPlatformRegistryManager
{

    /**
     * Gives access to the current platform's registry.
     *
     * @return The registry manager for the current platform.
     */
    static IPlatformRegistryManager getInstance() {
        return IChiselsAndBitsPlatformCore.getInstance().getPlatformRegistryManager();
    }

    /**
     * Gives access to the deferred registrar manager.
     *
     * @return The deferred registrar manager.
     */
    IRegistrarManager getRegistrarManager();

    /**
     * Gives access to the item registry.
     *
     * @return The item registry of the current game.
     */
    IPlatformRegistry<Item> getItemRegistry();

    /**
     * Gives access to the block registry.
     *
     * @return The block registry of the current game.
     */
    IPlatformRegistry<Block> getBlockRegistry();

    /**
     * Gives access to the global blockstate id map.
     *
     * @return The id to object map for blockstates.
     */
    ISizedIdMap<BlockState> getBlockStateIdMap();

    /**
     * Gives access to the fluid registry.
     *
     * @return The fluid registry of the current game.
     */
    IPlatformRegistry<Fluid> getFluids();
}
