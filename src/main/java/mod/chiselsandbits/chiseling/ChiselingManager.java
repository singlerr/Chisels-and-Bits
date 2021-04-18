package mod.chiselsandbits.chiseling;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.IChiselingManager;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.UUID;

public class ChiselingManager implements IChiselingManager
{
    private static final ChiselingManager INSTANCE = new ChiselingManager();

    private UUID activeInstanceId = UUID.randomUUID();

    private final ThreadLocal<UUID> activeThreadId = ThreadLocal.withInitial(() -> activeInstanceId);

    private final ThreadLocal<Table<UUID, ResourceLocation, IChiselingContext>> contexts = ThreadLocal.withInitial(HashBasedTable::create);
    private final ThreadLocal<Table<UUID, ResourceLocation, Long>> lastUsedChiselMoments = ThreadLocal.withInitial(HashBasedTable::create);

    private ChiselingManager()
    {
    }

    public static ChiselingManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Gets or creates a new chiseling context for the given player.
     * <p>
     * A new context is created when either one of the following conditions is met: - No context has been created before. - The world of the player and the world of the existing
     * context are not equal - The new chisel mode and the chisel mode of the existing context are not equal.
     *
     * @param playerEntity The player for which the context is looked up.
     * @param mode         The mode which the player wants to chisel in.
     * @return The context.
     */
    @Override
    public IChiselingContext getOrCreateContext(
      final PlayerEntity playerEntity, final IChiselMode mode)
    {
        validateOrSetup();

        final Map<ResourceLocation, IChiselingContext> currentPlayerContexts = Maps.newHashMap(contexts.get().row(playerEntity.getUniqueID()));
        for (final ResourceLocation worldId : currentPlayerContexts.keySet())
        {
            if (!worldId.equals(playerEntity.getEntityWorld().getDimensionKey().getLocation()))
            {
                contexts.get().remove(playerEntity.getUniqueID(), worldId);
            }
        }

        if (contexts.get().contains(playerEntity.getUniqueID(), playerEntity.getEntityWorld().getDimensionKey().getLocation()))
        {
            final IChiselingContext context = contexts.get().get(playerEntity.getUniqueID(), playerEntity.getEntityWorld().getDimensionKey().getLocation());
            if (context.getMode() == mode)
            {
                return context;
            }

            contexts.get().remove(playerEntity.getUniqueID(), playerEntity.getEntityWorld().getDimensionKey().getLocation());
        }

        final ChiselingContext newContext = new ChiselingContext(playerEntity.getEntityWorld(),
          mode,
          () -> {
            this.lastUsedChiselMoments.get().put(playerEntity.getUniqueID(), playerEntity.getEntityWorld().getDimensionKey().getLocation(), (long) playerEntity.ticksExisted);
            contexts.get().remove(playerEntity.getUniqueID(), playerEntity.getEntityWorld().getDimensionKey().getLocation());
        });

        contexts.get().put(playerEntity.getUniqueID(), playerEntity.getEntityWorld().getDimensionKey().getLocation(), newContext);

        return newContext;
    }

    public boolean canChisel(final PlayerEntity playerEntity) {
        validateOrSetup();

        final UUID playerId = playerEntity.getUniqueID();
        final ResourceLocation worldId = playerEntity.getEntityWorld().getDimensionKey().getLocation();

        final Long lastChiselTime = this.lastUsedChiselMoments.get().get(playerId, worldId);
        if (lastChiselTime == null)
            return true;

        final long time = playerEntity.ticksExisted;
        final long diffSinceLastUse = time - lastChiselTime;

        if (diffSinceLastUse > Constants.TICKS_BETWEEN_CHISEL_USAGE) {
            this.lastUsedChiselMoments.get().remove(playerId, worldId);
            return true;
        }

        return false;
    }

    public void onServerStarting()
    {
        this.activeInstanceId = UUID.randomUUID();
    }

    private void validateOrSetup() {
        final UUID threadId = activeThreadId.get();
        if (threadId != activeInstanceId) {
            this.contexts.get().clear();
            this.lastUsedChiselMoments.get().clear();

            activeThreadId.set(activeInstanceId);
        }
    }
}
