package mod.chiselsandbits.config;

import mod.chiselsandbits.api.config.ICommonConfiguration;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.platforms.core.config.ConfigurationType;
import mod.chiselsandbits.platforms.core.config.IConfigurationBuilder;
import mod.chiselsandbits.platforms.core.config.IConfigurationManager;

import java.util.function.Supplier;

public class CommonConfiguration implements ICommonConfiguration
{

    private final Supplier<Boolean> enableHelp;
    private final Supplier<Long> collisionBoxCacheSize;

    public CommonConfiguration() {
        final IConfigurationBuilder builder = IConfigurationManager.getInstance().createBuilder(
          ConfigurationType.NOT_SYNCED, Constants.MOD_ID + "-common"
        );

        enableHelp = builder.defineBoolean("help.enabled-in-tooltips", true);
        collisionBoxCacheSize = builder.defineInt("performance.caches.sizes.collision-boxes", 10000, 0, Long.MAX_VALUE);

        builder.setup();
    }

    @Override
    public Supplier<Boolean> getEnableHelp()
    {
        return enableHelp;
    }

    @Override
    public Supplier<Long> getCollisionBoxCacheSize()
    {
        return collisionBoxCacheSize;
    }
}
