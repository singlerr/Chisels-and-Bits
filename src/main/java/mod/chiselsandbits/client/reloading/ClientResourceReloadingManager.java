package mod.chiselsandbits.client.reloading;

import com.google.common.collect.Sets;
import mod.chiselsandbits.api.reloading.ICacheClearingHandler;
import mod.chiselsandbits.client.besr.BitStorageBESR;
import mod.chiselsandbits.client.model.baked.bit.BitBlockBakedModelManager;
import mod.chiselsandbits.client.model.baked.chiseled.ChiseledBlockBakedModelManager;
import mod.chiselsandbits.client.model.baked.face.FaceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ClientResourceReloadingManager implements IResourceManagerReloadListener
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ClientResourceReloadingManager INSTANCE = new ClientResourceReloadingManager();

    public static ClientResourceReloadingManager getInstance()
    {
        return INSTANCE;
    }

    private final Set<ICacheClearingHandler> cacheClearingHandlers = Sets.newConcurrentHashSet();

    private ClientResourceReloadingManager()
    {
    }

    @Override
    public void onResourceManagerReload(final @NotNull IResourceManager manager)
    {
        LOGGER.info("Resetting client caches");
        cacheClearingHandlers.forEach(ICacheClearingHandler::clear);
    }

    public ClientResourceReloadingManager registerCacheClearer(final ICacheClearingHandler cacheClearingHandler) {
        this.cacheClearingHandlers.add(cacheClearingHandler);
        return this;
    }

    public static void setup() {
        LOGGER.info("Setting up client reloading resource manager.");
        IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        if (resourceManager instanceof IReloadableResourceManager) {
            IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) resourceManager;
            reloadableResourceManager.registerReloadListener(ClientResourceReloadingManager.getInstance());
        }

        ClientResourceReloadingManager.getInstance()
          .registerCacheClearer(BitStorageBESR::clearCache)
          .registerCacheClearer(BitBlockBakedModelManager.getInstance()::clearCache)
          .registerCacheClearer(ChiseledBlockBakedModelManager.getInstance()::clearCache)
          .registerCacheClearer(FaceManager.getInstance()::clearCache);
    }
}
