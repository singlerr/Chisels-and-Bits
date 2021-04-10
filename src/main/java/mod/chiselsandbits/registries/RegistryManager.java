package mod.chiselsandbits.registries;

import mod.chiselsandbits.api.chiseling.IChiselMode;
import mod.chiselsandbits.api.registries.IRegistryManager;
import mod.chiselsandbits.registrars.ModChiselModes;
import net.minecraftforge.registries.IForgeRegistry;

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
}
