package mod.chiselsandbits.plugin;

import com.google.common.collect.ImmutableSet;
import mod.chiselsandbits.api.plugin.ChiselsAndBitsPlugin;
import mod.chiselsandbits.api.plugin.IChiselsAndBitsPlugin;
import mod.chiselsandbits.api.plugin.IPluginManager;
import mod.chiselsandbits.api.plugin.IPluginDiscoverer;

import java.util.function.Consumer;

public final class PluginManger implements IPluginManager
{
    private static final PluginManger INSTANCE = new PluginManger();

    public static PluginManger getInstance()
    {
        return INSTANCE;
    }

    private ImmutableSet<IChiselsAndBitsPlugin> plugins = ImmutableSet.of();

    private PluginManger()
    {
    }

    @Override
    public ImmutableSet<IChiselsAndBitsPlugin> getPlugins()
    {
        return plugins;
    }

    @Override
    public void run(Consumer<IChiselsAndBitsPlugin> callback) {
        getPlugins().forEach(callback);
    }

    public void detect() {
        this.plugins = ImmutableSet.copyOf(IPluginDiscoverer.getInstance().loadPlugins(
          ChiselsAndBitsPlugin.class,
          ChiselsAndBitsPlugin.Instance.class,
          IChiselsAndBitsPlugin.class,
          IChiselsAndBitsPlugin::getId
        ));
    }
}
