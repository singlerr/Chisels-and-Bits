package mod.chiselsandbits.fabric.integration.rei;

import mod.chiselsandbits.platforms.core.config.ConfigurationType;
import mod.chiselsandbits.platforms.core.config.IConfigurationBuilder;
import mod.chiselsandbits.platforms.core.config.IConfigurationManager;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class REICompatConfiguration
{
    private static final Logger                 LOGGER   = LogManager.getLogger();
    private static final REICompatConfiguration INSTANCE = new REICompatConfiguration();

    public static REICompatConfiguration getInstance()
    {
        return INSTANCE;
    }

    private final Supplier<Boolean> injectBits;

    private REICompatConfiguration()
    {
        final IConfigurationBuilder builder = IConfigurationManager.getInstance().createBuilder(ConfigurationType.NOT_SYNCED, Constants.MOD_ID + "-client-compat-" + REIChiselsAndBitsPlugin.ID);

        injectBits = builder.defineBoolean("compat.jei.inject-bits", true);

        builder.setup();
    }

    public void initialize()
    {
        LOGGER.info("Loaded JEI Compatibility configuration");
    }

    public Supplier<Boolean> getInjectBits()
    {
        return injectBits;
    }
}
