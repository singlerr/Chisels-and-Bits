package mod.chiselsandbits.fabric.plugin;

import com.google.common.collect.ImmutableSet;
import mod.chiselsandbits.api.plugin.IPluginDiscoverer;
import mod.chiselsandbits.api.plugin.PluginData;
import mod.chiselsandbits.api.util.GroupingUtils;
import mod.chiselsandbits.fabric.plugin.asm.FabricPluginDiscoverer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FabricPluginManager implements IPluginDiscoverer
{
    private static final FabricPluginManager INSTANCE = new FabricPluginManager();

    public static FabricPluginManager getInstance()
    {
        return INSTANCE;
    }

    private FabricPluginManager()
    {
    }

    @Override
    public <A, I extends Annotation, T> Collection<PluginData<T>> loadPlugins(
      final Class<A> annotationType, final Class<I> instanceAnnotationType, final Class<T> pluginSpecificationType, final Function<T, String> idExtractor)
    {
        final IPluginDiscoverer asmBasedDiscoverer = new FabricPluginDiscoverer();
        return asmBasedDiscoverer.loadPlugins(annotationType, instanceAnnotationType, pluginSpecificationType, idExtractor);
    }
}
