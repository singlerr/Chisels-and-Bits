package mod.chiselsandbits.api.registries;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Manages all registries which are used by Chisels and Bits.
 */
public interface IRegistryManager
{

    static IRegistryManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getRegistryManager();
    }

    /**
     * The registry which controls all available chiseling modes.
     *
     * @return The registry.
     */
    IForgeRegistry<IChiselMode> getChiselModeRegistry();
}
