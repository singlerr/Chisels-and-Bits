package mod.chiselsandbits.chiseledblock.data;

import mod.chiselsandbits.api.BoxType;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.util.math.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public final class VoxelShapeCache
{

    private static final VoxelShapeCache INSTANCE = new VoxelShapeCache();

    public static VoxelShapeCache getInstance()
    {
        return INSTANCE;
    }

    private final LinkedHashMap<CacheKey, VoxelShape> cache = new LinkedHashMap<>();

    private VoxelShapeCache()
    {
    }

    public VoxelShape get(VoxelBlob blob, BoxType type) {
        final List<Boolean> keyList = new ArrayList<>(blob.noneAir.length);
        for (final boolean b : blob.noneAir)
        {
            keyList.add(b);
        }
        final CacheKey key = new CacheKey(type, keyList);

        VoxelShape shape = cache.get(key);
        evictFromCacheIfNeeded();
        if (shape == null)
            shape = calculateNewVoxelShape(blob, type);

        cache.put(key, shape);
        return shape;
    }

    private VoxelShape calculateNewVoxelShape(final VoxelBlob data, final BoxType type) {
        return VoxelShapeCalculator.calculate(data, type).simplify();
    }

    private void evictFromCacheIfNeeded() {
        if (cache.size() == ChiselsAndBits.getConfig().getCommon().collisionBoxCacheSize.get()) {
            cache.remove(cache.keySet().iterator().next());
        }
    }

    private static final class CacheKey {
        private final BoxType type;
        private final List<Boolean> bbList;

        private CacheKey(final BoxType type, final List<Boolean> bbList) {
            this.type = type;
            this.bbList = bbList;
        }

        public BoxType getType()
        {
            return type;
        }

        public List<Boolean> getBbList()
        {
            return bbList;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            final CacheKey cacheKey = (CacheKey) o;
            return getType() == cacheKey.getType() &&
                     getBbList().equals(cacheKey.getBbList());
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(getType(), getBbList());
        }
    }
}
