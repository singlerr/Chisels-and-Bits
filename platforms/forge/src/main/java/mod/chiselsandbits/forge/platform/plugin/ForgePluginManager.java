package mod.chiselsandbits.forge.platform.plugin;

import com.google.common.collect.ImmutableSet;
import mod.chiselsandbits.api.util.ClassUtils;
import mod.chiselsandbits.api.util.GroupingUtils;
import mod.chiselsandbits.platforms.core.plugin.IPlatformPluginManager;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ForgePluginManager implements IPlatformPluginManager
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ForgePluginManager INSTANCE = new ForgePluginManager();

    public static ForgePluginManager getInstance()
    {
        return INSTANCE;
    }

    private ForgePluginManager()
    {
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A, I extends Annotation, T> Collection<T> loadPlugins(
      final Class<A> annotationType,
      final Class<I> instanceAnnotationType,
      final Class<T> pluginSpecificationType,
      final Function<T, String> idExtractor
    )
    {
        Type pluginType = Type.getType(annotationType);

        ModList modList = ModList.get();
        List<T> plugins = new ArrayList<>();
        for (ModFileScanData scanData : modList.getAllScanData()) {
            for (ModFileScanData.AnnotationData data : scanData.getAnnotations()) {
                if (pluginType.equals(data.annotationType())) {
                    final ArrayList<String> requiredMods = (ArrayList<String>) data.annotationData().get("requiredMods");
                    if (requiredMods != null && requiredMods.size() > 0) {
                        if (requiredMods.stream().anyMatch(modId -> !ModList.get().isLoaded(modId))) {
                            continue;
                        }
                    }

                    T plugin = createPluginFrom(
                      data.memberName(),
                      pluginSpecificationType,
                      instanceAnnotationType,
                      idExtractor
                    );

                    if (plugin != null) {
                        plugins.add(plugin);
                        LOGGER.info("Found and loaded ChiselsAndBits plugin: {}", idExtractor.apply(plugin));
                    }
                }
            }
        }

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

    @Nullable
    private static <T, I extends Annotation> T createPluginFrom(
      String className,
      final Class<T> pluginSpecificationType,
      final Class<I> instanceAnnotationType,
      final Function<T, String> idExtractor
    ) {
        return ClassUtils.createOrGetInstance(
          className,
          pluginSpecificationType,
          instanceAnnotationType,
          idExtractor
        );
    }
}
