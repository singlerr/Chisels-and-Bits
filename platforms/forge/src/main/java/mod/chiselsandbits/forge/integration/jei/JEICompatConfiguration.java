/*
package mod.chiselsandbits.forge.integration.jei;

import mezz.jei.api.constants.ModIds;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.platforms.core.config.ConfigurationType;
import mod.chiselsandbits.platforms.core.config.IConfigurationBuilder;
import mod.chiselsandbits.platforms.core.config.IConfigurationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class JEICompatConfiguration
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final JEICompatConfiguration INSTANCE = new JEICompatConfiguration();

    public static JEICompatConfiguration getInstance()
    {
        return INSTANCE;
    }

    private final Supplier<Boolean> injectBits;

    private JEICompatConfiguration()
    {
        final IConfigurationBuilder builder = IConfigurationManager.getInstance().createBuilder(ConfigurationType.NOT_SYNCED, Constants.MOD_ID + "-client-compat-" + ModIds.JEI_ID);

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
*/
