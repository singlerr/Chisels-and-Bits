package mod.chiselsandbits.client.model.baked.chiseled;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IAreaShapeIdentifier;
import mod.chiselsandbits.client.model.baked.simple.NullBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ChiseledBlockBakedModelManager
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final ChiseledBlockBakedModelManager INSTANCE = new ChiseledBlockBakedModelManager();

    public static ChiseledBlockBakedModelManager getInstance()
    {
        return INSTANCE;
    }

    private final Cache<Key, ChiseledBlockBakedModel> cache = CacheBuilder.newBuilder()
        .maximumSize(Configuration.getInstance().getClient().modelCacheSize.get())
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build();

    private ChiseledBlockBakedModelManager()
    {
    }

    public ChiseledBlockBakedModel get(
      final IAreaAccessor accessor,
      final BlockState primaryState,
      final ChiselRenderType renderType
    ) {
        final Key key = new Key(accessor.createNewShapeIdentifier(), primaryState, renderType);
        try
        {
            return cache.get(key, () -> new ChiseledBlockBakedModel(primaryState, renderType, accessor));
        }
        catch (ExecutionException e)
        {
            LOGGER.error("Failed to calculate the chiseled block model. Calculation was interrupted.", e);
            return ChiseledBlockBakedModel.EMPTY;
        }
    }

    public Optional<ChiseledBlockBakedModel> get(
      final ItemStack itemStack,
      final ChiselRenderType renderType
    ) {
        final Item item = itemStack.getItem();
        if (!(item instanceof IMultiStateItem))
            return Optional.empty();

        final IMultiStateItem multiStateItem = (IMultiStateItem) item;
        final IMultiStateItemStack multiStateItemStack = multiStateItem.createItemStack(itemStack);

        return Optional.of(
          get(
            multiStateItemStack,
            multiStateItemStack.getStatistics().getPrimaryState(),
            renderType
          )
        );
    }

    private static final class Key {
        private final IAreaShapeIdentifier identifier;
        private final BlockState primaryState;
        private final ChiselRenderType renderType;

        private Key(final IAreaShapeIdentifier identifier, final BlockState primaryState, final ChiselRenderType renderType) {
            this.identifier = identifier;
            this.primaryState = primaryState;
            this.renderType = renderType;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof Key))
            {
                return false;
            }
            final Key key = (Key) o;
            return Objects.equals(identifier, key.identifier) && Objects.equals(primaryState, key.primaryState) && renderType == key.renderType;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(identifier, primaryState, renderType);
        }
    }
}
