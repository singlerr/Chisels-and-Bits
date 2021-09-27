package mod.chiselsandbits.voxelshape;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

public class VoxelShapeManager implements IVoxelShapeManager
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final VoxelShapeManager INSTANCE = new VoxelShapeManager();

    private final Cache<Key, VoxelShape> cache = CacheBuilder.newBuilder()
                                                                           .expireAfterAccess(5, TimeUnit.MINUTES)
                                                                           .build();

    private VoxelShapeManager()
    {
    }

    public static VoxelShapeManager getInstance()
    {
        return INSTANCE;
    }

    @Override
    public VoxelShape get(
      final IAreaAccessor accessor,
      final BlockPos offset,
      final Function<IAreaAccessor, Predicate<IStateEntryInfo>> selectablePredicateBuilder,
      final boolean simplify)
    {
        try {
            final Key cacheKey = new Key(
              accessor.createNewShapeIdentifier(),
              offset,
              selectablePredicateBuilder.apply(accessor),
              simplify);

            return cache.get(cacheKey,
              () -> {
                final VoxelShape calculatedShape = VoxelShapeCalculator.calculate(accessor, offset, selectablePredicateBuilder, simplify);
                if (calculatedShape.isEmpty())
                    return VoxelShapes.empty();

                return calculatedShape;
            });
        }
        catch (ExecutionException e)
        {
            LOGGER.warn("Failed to calculate voxelshape.", e);
            return VoxelShapes.empty();
        }
    }

    @Override
    public Optional<VoxelShape> getCached(
      final IAreaShapeIdentifier identifier,
      final BlockPos offset,
      final Predicate<IStateEntryInfo> selectablePredicate,
      final boolean simplify)
    {
        final Key key = new Key(
          identifier,
          offset, selectablePredicate,
          simplify);

        return Optional.ofNullable(cache.getIfPresent(key));
    }

    public void clearCache()
    {
        this.cache.asMap().clear();
    }

    private static final class Key {
        private final IAreaShapeIdentifier identifier;
        private final BlockPos offset;
        private final Predicate<IStateEntryInfo> predicate;
        private final boolean simplify;

        private Key(final IAreaShapeIdentifier identifier, final BlockPos offset, final Predicate<IStateEntryInfo> predicate, final boolean simplify) {
            this.identifier = identifier;
            this.offset = offset;
            this.predicate = predicate;
            this.simplify = simplify;
        }

        @Override
        public String toString()
        {
            return "Key{" +
                     "identifier=" + identifier.toString() +
                     ", offset=" + offset +
                     ", predicate=" + predicate +
                     ", simplify=" + simplify +
                     '}';
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

            if (simplify != key.simplify)
            {
                return false;
            }
            if (!Objects.equals(identifier, key.identifier))
            {
                return false;
            }
            if (!Objects.equals(offset, key.offset))
            {
                return false;
            }
            return Objects.equals(predicate, key.predicate);
        }

        @Override
        public int hashCode()
        {
            int result = identifier != null ? identifier.hashCode() : 0;
            result = 31 * result + (offset != null ? offset.hashCode() : 0);
            result = 31 * result + (predicate != null ? predicate.hashCode() : 0);
            result = 31 * result + (simplify ? 1 : 0);
            return result;
        }
    }
}
