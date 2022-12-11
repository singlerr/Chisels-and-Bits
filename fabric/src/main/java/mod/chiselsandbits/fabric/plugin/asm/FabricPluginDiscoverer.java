package mod.chiselsandbits.fabric.plugin.asm;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import mod.chiselsandbits.api.config.ICommonConfiguration;
import mod.chiselsandbits.api.launch.ILaunchPropertyManager;
import mod.chiselsandbits.api.plugin.IPluginDiscoverer;
import mod.chiselsandbits.api.plugin.PluginData;
import mod.chiselsandbits.api.util.ClassUtils;
import mod.chiselsandbits.api.util.GroupingUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.commons.io.file.AccumulatorPathVisitor;
import org.apache.commons.io.file.Counters;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.util.asm.ASM;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FabricPluginDiscoverer implements IPluginDiscoverer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricPluginDiscoverer.class);

    private record DiscoveredPlugin(Map<String, Object> annotationData, String className) {};

    public <A, I extends Annotation, T> Collection<PluginData<T>> loadPlugins(Class<A> annotationType, Class<I> instanceAnnotationType, Class<T> pluginSpecificationType, Function<T, String> idExtractor) {
        final Set<DiscoveredPlugin> pluginCandidates = Sets.newHashSet();

        for (ModContainer allMod : FabricLoader.getInstance().getAllMods()) {
            for (Path rootPath : allMod.getRootPaths()) {
                final AccumulatorPathVisitor visitor = new AccumulatorPathVisitor(Counters.noopPathCounters(), new RegexFileFilter("*.class"), TrueFileFilter.TRUE);
                try {
                    Files.walkFileTree(rootPath, visitor);
                } catch (IOException e) {
                    LOGGER.warn("Failed to discover plugins from path: %s".formatted(rootPath), e);
                    continue;
                }

                final List<Path> classFiles = visitor.getFileList();
                for (Path classFile : classFiles) {
                    final ClassReader reader;
                    try {
                        reader = new ClassReader(Files.newInputStream(classFile));
                    } catch (IOException e) {
                        LOGGER.warn("Failed to read class for plugin detection: %s".formatted(classFile.toAbsolutePath()), e);
                        continue;
                    }

                    reader.accept(new AnnotationSearchingClassVisitor(pluginCandidates, annotationType), ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                }
            }
        }


        List<PluginData<T>> plugins = new ArrayList<>();
        for (DiscoveredPlugin pluginCandidate : pluginCandidates) {
            final ArrayList<String> requiredMods = (ArrayList<String>) pluginCandidate.annotationData().get("requiredMods");
            if (requiredMods != null && requiredMods.size() > 0) {
                if (requiredMods.stream().anyMatch(modId -> !FabricLoader.getInstance().isModLoaded(modId))) {
                    continue;
                }
            }

            final Boolean isExperimental = (Boolean) pluginCandidate.annotationData().get("isExperimental");
            if (isExperimental != null && isExperimental && !Boolean.parseBoolean(ILaunchPropertyManager.getInstance().get("plugins.experimental", "false"))) {
                continue;
            }

            T plugin = createPluginFrom(
                    pluginCandidate.className(),
                    pluginSpecificationType,
                    instanceAnnotationType,
                    idExtractor
            );

            if (plugin != null) {
                plugins.add(new PluginData<>(plugin, isExperimental != null && isExperimental));
                LOGGER.info("Found and loaded ChiselsAndBits plugin: {}", idExtractor.apply(plugin));
            }
        }

        final Collection<Collection<T>> groupedByIds = GroupingUtils.groupByUsingSet(plugins.stream().map(PluginData::plugin).collect(Collectors.toList()), idExtractor);
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


    private static final class AnnotationSearchingClassVisitor extends ClassVisitor {

        private final Set<DiscoveredPlugin> pluginCandidates;
        private final Class<?> annotationType;
        private final Map<String, Object> payloadCandidate = Maps.newHashMap();
        private String name = "";

        public AnnotationSearchingClassVisitor(Set<DiscoveredPlugin> pluginCandidates, Class<?> annotationType) {
            super(ASM.API_VERSION);
            this.pluginCandidates = pluginCandidates;
            this.annotationType = annotationType;
        }



        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.name = name;
        }

        @Override
        public void visitEnd() {
            if (!payloadCandidate.isEmpty() && !name.isBlank()) {
                pluginCandidates.add(new DiscoveredPlugin(payloadCandidate, name));
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (!annotationType.getName().equals(descriptor))
                return super.visitAnnotation(descriptor, visible);

            return new AnnotationVisitor(ASM.API_VERSION, super.visitAnnotation(descriptor, visible))
            {
                @Override
                public void visit(String name, Object value) {
                    super.visit(name, value);
                    payloadCandidate.put(name, value);
                }

                @SuppressWarnings({"unchecked", "rawtypes"})
                @Override
                public void visitEnum(String name, String descriptor, String value) {
                    super.visitEnum(name, descriptor, value);
                    try {
                        final Class<? extends Enum> enumClass = (Class<? extends Enum>) Class.forName(descriptor);
                        payloadCandidate.put(name, Enum.valueOf(enumClass, value));
                    } catch (ClassNotFoundException | ClassCastException e) {
                        LOGGER.warn("Failed to handle payload enum: " + descriptor);
                    }
                }

                @Override
                public AnnotationVisitor visitArray(String name) {
                    return super.visitArray(name);
                }
            };
        }
    }
}
