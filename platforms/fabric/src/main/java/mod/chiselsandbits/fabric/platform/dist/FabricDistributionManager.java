package mod.chiselsandbits.fabric.platform.dist;

import mod.chiselsandbits.platforms.core.dist.Dist;
import mod.chiselsandbits.platforms.core.dist.IDistributionManager;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricDistributionManager implements IDistributionManager
{
    private static final FabricDistributionManager INSTANCE = new FabricDistributionManager();

    public static FabricDistributionManager getInstance()
    {
        return INSTANCE;
    }

    private FabricDistributionManager()
    {
    }

    @Override
    public Dist getCurrentDistribution()
    {
        return switch (FabricLoader.getInstance().getEnvironmentType()) {
            case CLIENT -> Dist.CLIENT;
            case SERVER -> Dist.DEDICATED_SERVER;
        };
    }

    @Override
    public boolean isProduction()
    {
        return true;
    }
}
