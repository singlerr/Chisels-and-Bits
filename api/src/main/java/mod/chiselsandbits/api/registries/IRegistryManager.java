package mod.chiselsandbits.api.registries;

import com.communi.suggestu.scena.core.registries.ICustomRegistry;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.cutting.operation.ICuttingOperation;
import mod.chiselsandbits.api.glueing.operation.IGlueingOperation;
import mod.chiselsandbits.api.modification.operation.IModificationOperation;
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
    ICustomRegistry<IChiselMode> getChiselModeRegistry();

    /**
     * The registry used for modifications of single use patterns.
     *
     * @return The modification operation registry.
     */
    @NotNull
    ICustomRegistry<IModificationOperation> getModificationOperationRegistry();

    /**
     * The registry used for cutting of single use patterns.
     *
     * @return The cutting operation registry.
     */
    @NotNull
    ICustomRegistry<ICuttingOperation> getCuttingOperationRegistry();

    /**
     * The registry used for glueing of single use patterns.
     *
     * @return The glueing operation registry.
     */
    @NotNull
    ICustomRegistry<IGlueingOperation> getGlueingOperationRegistry();
}
