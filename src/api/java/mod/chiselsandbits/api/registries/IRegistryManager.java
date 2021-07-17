package mod.chiselsandbits.api.registries;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.modification.operation.IModificationTableOperation;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

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

    /**
     * The forge registry used for modifications of single use patterns.
     *
     * @return The modification table operation registry.
     */
    @NotNull
    IForgeRegistry<IModificationTableOperation> getModificationTableOperationRegistry();
}
