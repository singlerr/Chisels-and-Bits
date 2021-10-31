package mod.chiselsandbits.reloading;

import com.google.common.collect.Sets;
import mod.chiselsandbits.aabb.AABBManager;
import mod.chiselsandbits.api.reloading.ICacheClearingHandler;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.change.ChangeTrackerManger;
import mod.chiselsandbits.chiseling.LocalChiselingContextCache;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Unit;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DataReloadingResourceManager implements IFutureReloadListener
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DataReloadingResourceManager INSTANCE = new DataReloadingResourceManager();

    public static DataReloadingResourceManager getInstance()
    {
        return INSTANCE;
    }

    @SubscribeEvent
    public static void registerReloadListener(final AddReloadListenerEvent addReloadListenerEvent) {
        LOGGER.info("Setting up data reloading resource manager.");
        addReloadListenerEvent.addListener(DataReloadingResourceManager.getInstance());
        DataReloadingResourceManager.getInstance().setup();
    }

    private final Set<ICacheClearingHandler> cacheClearingHandlers = Sets.newConcurrentHashSet();

    private DataReloadingResourceManager()
    {
    }

    public DataReloadingResourceManager registerCacheClearer(final ICacheClearingHandler cacheClearingHandler) {
        this.cacheClearingHandlers.add(cacheClearingHandler);
        return this;
    }

    public void onResourceManagerReload()
    {
        LOGGER.info("Resetting common caches");
        this.cacheClearingHandlers.forEach(ICacheClearingHandler::clear);
    }

    private void setup() {
        this.cacheClearingHandlers.clear();

        registerCacheClearer(AABBManager.getInstance()::clearCache)
          .registerCacheClearer(VoxelShapeManager.getInstance()::clearCache)
          .registerCacheClearer(LocalChiselingContextCache.getInstance()::clearCache)
          .registerCacheClearer(ChangeTrackerManger.getInstance()::clearCache);
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(
      final IStage stage,
      final @NotNull IResourceManager manager,
      final @NotNull IProfiler preparationProfiler,
      final @NotNull IProfiler reloadProfiler,
      final @NotNull Executor backgroundExecutor,
      final @NotNull Executor gameExecutor)
    {
        return stage.wait(Unit.INSTANCE).thenRunAsync(() -> {
            reloadProfiler.startTick();
            reloadProfiler.push("C&B Data reload");
            this.onResourceManagerReload();
            reloadProfiler.pop();
            reloadProfiler.endTick();
        }, gameExecutor);
    }
}
