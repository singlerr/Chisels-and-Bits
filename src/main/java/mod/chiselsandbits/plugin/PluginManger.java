package mod.chiselsandbits.plugin;

import com.google.common.collect.ImmutableSet;
import mod.chiselsandbits.api.plugin.ChiselsAndBitsPlugin;
import mod.chiselsandbits.api.plugin.IChiselsAndBitsPlugin;
import mod.chiselsandbits.api.plugin.IChiselsAndBitsPluginManager;
import mod.chiselsandbits.api.util.ClassUtils;
import mod.chiselsandbits.api.util.GroupingUtils;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class PluginManger implements IChiselsAndBitsPluginManager
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Type AEQUIVALEO_PLUGIN_TYPE = Type.getType(ChiselsAndBitsPlugin.class);

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

    public void run(Consumer<IChiselsAndBitsPlugin> callback) {
        getPlugins().forEach(callback);
    }

    @SuppressWarnings("unchecked")
    public void detect() {
        ModList modList = ModList.get();
        List<IChiselsAndBitsPlugin> plugins = new ArrayList<>();
        for (ModFileScanData scanData : modList.getAllScanData()) {
            for (ModFileScanData.AnnotationData data : scanData.getAnnotations()) {
                if (AEQUIVALEO_PLUGIN_TYPE.equals(data.getAnnotationType())) {
                    final ArrayList<String> requiredMods = (ArrayList<String>) data.getAnnotationData().get("requiredMods");
                    if (requiredMods != null && requiredMods.size() > 0) {
                        if (requiredMods.stream().anyMatch(modId -> !ModList.get().isLoaded(modId))) {
                            continue;
                        }
                    }

                    IChiselsAndBitsPlugin plugin = createPluginFrom(data.getMemberName());
                    if (plugin != null) {
                        plugins.add(plugin);
                        LOGGER.info("Found and loaded ChiselsAndBits plugin: {}", plugin.getId());
                    }
                }
            }
        }

        final Collection<Collection<IChiselsAndBitsPlugin>> groupedByIds = GroupingUtils.groupByUsingSet(plugins, IChiselsAndBitsPlugin::getId);
        final Collection<String> idsWithDuplicates = groupedByIds.stream().filter(p -> p.size() > 1).map(p -> p.iterator().next()).map(IChiselsAndBitsPlugin::getId).collect(Collectors.toSet());
        if (idsWithDuplicates.size() > 0) {
            throw new RuntimeException(String.format("Can not load C&B there are multiple instances of the plugins: [%s]", String.join(", ", idsWithDuplicates)));
        }

        this.plugins = ImmutableSet.copyOf(plugins);
    }

    @Nullable
    private static IChiselsAndBitsPlugin createPluginFrom(String className) {
        return ClassUtils.createOrGetInstance(className, IChiselsAndBitsPlugin.class, ChiselsAndBitsPlugin.Instance.class, IChiselsAndBitsPlugin::getId);
    }
}
