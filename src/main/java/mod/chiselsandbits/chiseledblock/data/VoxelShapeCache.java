package mod.chiselsandbits.chiseledblock.data;

import com.google.common.collect.Lists;
import com.sun.org.apache.xpath.internal.operations.Bool;
import mod.chiselsandbits.api.BoxType;
import mod.chiselsandbits.config.CommonConfiguration;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

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
            shape = calculateNewVoxelShape(blob, keyList);

        cache.put(key, shape);
        return shape;
    }

    private VoxelShape calculateNewVoxelShape(final VoxelBlob data, final List<Boolean> noneAirList) {
        return VoxelShapeCalculator.calculate(data, noneAirList).simplify();
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
