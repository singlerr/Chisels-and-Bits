package mod.chiselsandbits.registries;

import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.modification.operation.IModificationTableOperation;
import mod.chiselsandbits.api.registries.IRegistryManager;
import mod.chiselsandbits.registrars.ModChiselModes;
import mod.chiselsandbits.registrars.ModModificationOperation;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

public class RegistryManager implements IRegistryManager
{
    private static final RegistryManager INSTANCE = new RegistryManager();

    private RegistryManager()
    {
    }

    public static RegistryManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * The registry which controls all available chiseling modes.
     *
     * @return The registry.
     */
    @Override
    public IForgeRegistry<IChiselMode> getChiselModeRegistry()
    {
        return ModChiselModes.REGISTRY.get();
    }

    @Override
    public @NotNull IForgeRegistry<IModificationTableOperation> getModificationTableOperationRegistry()
    {
        return ModModificationOperation.REGISTRY_SUPPLIER.get();
    }
}
