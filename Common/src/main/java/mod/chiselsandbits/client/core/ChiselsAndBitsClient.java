package mod.chiselsandbits.client.core;

import mod.chiselsandbits.api.plugin.IChiselsAndBitsPlugin;
import mod.chiselsandbits.plugin.PluginManger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ChiselsAndBitsClient
{

    private static final Logger LOGGER = LogManager.getLogger();

    public ChiselsAndBitsClient()
    {
        LOGGER.info("Loading chisels and bits client");
        PluginManger.getInstance().run(IChiselsAndBitsPlugin::onClientSetup);
    }
}
