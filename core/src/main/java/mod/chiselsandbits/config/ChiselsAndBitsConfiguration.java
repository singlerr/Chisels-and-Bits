package mod.chiselsandbits.config;

import mod.chiselsandbits.api.config.IChiselsAndBitsConfiguration;
import mod.chiselsandbits.api.config.IClientConfiguration;
import mod.chiselsandbits.api.config.ICommonConfiguration;
import mod.chiselsandbits.api.config.IServerConfiguration;

public class ChiselsAndBitsConfiguration implements IChiselsAndBitsConfiguration
{

    private final IClientConfiguration client = new ClientConfiguration();
    private final ICommonConfiguration common = new CommonConfiguration();
    private final IServerConfiguration server = new ServerConfiguration();

    @Override
    public IClientConfiguration getClient()
    {
        return client;
    }

    @Override
    public ICommonConfiguration getCommon()
    {
        return common;
    }

    @Override
    public IServerConfiguration getServer()
    {
        return server;
    }
}
