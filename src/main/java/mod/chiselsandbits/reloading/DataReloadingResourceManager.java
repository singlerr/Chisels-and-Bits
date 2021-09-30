package mod.chiselsandbits.reloading;

import com.google.common.collect.Sets;
import mod.chiselsandbits.aabb.AABBManager;
import mod.chiselsandbits.api.reloading.ICacheClearingHandler;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.change.ChangeTrackerManger;
import mod.chiselsandbits.chiseling.LocalChiselingContextCache;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DataReloadingResourceManager implements IResourceManagerReloadListener
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

    @Override
    public void onResourceManagerReload(final @NotNull IResourceManager manager)
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
}
