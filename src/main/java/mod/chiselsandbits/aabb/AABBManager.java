package mod.chiselsandbits.aabb;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class AABBManager
{
    private static final AABBManager INSTANCE = new AABBManager();

    public static AABBManager getInstance()
    {
        return INSTANCE;
    }

    private final SimpleMaxSizedCache<Key, Collection<AxisAlignedBB>> cache = new SimpleMaxSizedCache<>(
      IChiselsAndBitsAPI.getInstance().getConfiguration().getCommon().collisionBoxCacheSize::get
    );

    private AABBManager()
    {
    }

    public Collection<AxisAlignedBB> get(
      final IAreaAccessor accessor,
      final Function<IAreaAccessor, Predicate<IStateEntryInfo>> selectablePredicateBuilder
    ) {
        final Predicate<IStateEntryInfo> selectablePredicate = selectablePredicateBuilder.apply(accessor);

        final Key cacheKey = new Key(
          accessor.createNewShapeIdentifier(),
          selectablePredicate
        );

        return cache.get(
          cacheKey,
          () -> AABBCompressor.compressStates(accessor, selectablePredicate)
        );
    }

    public void clearCache()
    {
        this.cache.clear();
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
