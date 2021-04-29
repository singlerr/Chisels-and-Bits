package mod.chiselsandbits.aabb;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class AABBManager
{
    private static final AABBManager INSTANCE = new AABBManager();
    private static final Logger LOGGER = LogManager.getLogger();

    public static AABBManager getInstance()
    {
        return INSTANCE;
    }

    private final Cache<Key, Collection<AxisAlignedBB>> cache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build();

    private AABBManager()
    {
    }

    public Collection<AxisAlignedBB> get(
      final IAreaAccessor accessor,
      final Predicate<IStateEntryInfo> selectablePredicate
    ) {
        final Key cacheKey = new Key(
          accessor.createNewShapeIdentifier(),
          selectablePredicate
        );

        try
        {
            return cache.get(
              cacheKey,
              () -> AABBCompressor.compressStates(accessor, Vector3d.copy(accessor.getAreaOrigin()), selectablePredicate)
            );
        }
        catch (ExecutionException e)
        {
            LOGGER.warn("Failed to calculate the bounding boxes for an area.", e);
            return Collections.emptyList();
        }
    }

    private static final class Key {
        private final IAreaShapeIdentifier identifier;
        private final Predicate<IStateEntryInfo> predicate;

        private Key(final IAreaShapeIdentifier identifier, final Predicate<IStateEntryInfo> predicate) {
            this.identifier = identifier;
            this.predicate = predicate;
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
            return Objects.equals(identifier, key.identifier) && Objects.equals(predicate, key.predicate);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(identifier, predicate);
        }
    }
}
