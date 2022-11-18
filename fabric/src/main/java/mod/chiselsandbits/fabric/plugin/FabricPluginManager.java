package mod.chiselsandbits.fabric.plugin;

import com.google.common.collect.ImmutableSet;
import mod.chiselsandbits.api.plugin.IPluginDiscoverer;
import mod.chiselsandbits.api.util.GroupingUtils;
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
    public <A, I extends Annotation, T> Collection<T> loadPlugins(
      final Class<A> annotationType, final Class<I> instanceAnnotationType, final Class<T> pluginSpecificationType, final Function<T, String> idExtractor)
    {
        final List<T> plugins = FabricLoader.getInstance().getEntrypointContainers(
          "chiselsandbits:plugin", pluginSpecificationType
        ).stream().map(EntrypointContainer::getEntrypoint).collect(Collectors.toList());

        final Collection<Collection<T>> groupedByIds = GroupingUtils.groupByUsingSet(plugins, idExtractor);
        final Collection<String> idsWithDuplicates = groupedByIds.stream()
          .filter(p -> p.size() > 1)
          .map(p -> p.iterator().next())
          .map(idExtractor)
          .collect(Collectors.toSet());

        if (idsWithDuplicates.size() > 0) {
            throw new RuntimeException(String.format("Can not load C&B there are multiple instances of the plugins: [%s]", String.join(", ", idsWithDuplicates)));
        }

        return ImmutableSet.copyOf(plugins);
    }
}
