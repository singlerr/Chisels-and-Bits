package mod.chiselsandbits.chiseling;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.IChiselingManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.UUID;

public class ChiselingManager implements IChiselingManager
{
    private static final ChiselingManager INSTANCE = new ChiselingManager();
    private final ThreadLocal<Table<UUID, ResourceLocation, IChiselingContext>> contexts = ThreadLocal.withInitial(HashBasedTable::create);

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
          () -> contexts.get().remove(playerEntity.getUniqueID(),  playerEntity.getEntityWorld().getDimensionKey().getLocation()));

        contexts.get().put(playerEntity.getUniqueID(), playerEntity.getEntityWorld().getDimensionKey().getLocation(), newContext);

        return newContext;
    }
}
