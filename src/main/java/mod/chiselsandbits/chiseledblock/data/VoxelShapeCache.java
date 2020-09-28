package mod.chiselsandbits.chiseledblock.data;

import mod.chiselsandbits.api.BoxType;
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

    public VoxelShape get(VoxelBlobStateReference blobStateReference, BoxType type) {
        final Collection<AxisAlignedBB> boxes = blobStateReference.getBoxes(type);
        //noinspection ToArrayCallWithZeroLengthArrayArgument -> Not possible due to the way the BoxCollection operates.
        final AxisAlignedBB[] boxData = boxes.toArray(new AxisAlignedBB[boxes.size()]);
        final CacheKey key = new CacheKey(type, Arrays.asList(boxData));

        VoxelShape shape = cache.get(key);
        evictFromCacheIfNeeded();
        if (shape == null)
            shape = calculateNewVoxelShape(key.getBbList());

        cache.put(key, shape);
        return shape;
    }

    private VoxelShape calculateNewVoxelShape(final List<AxisAlignedBB> data) {
        return data.stream().reduce(
          VoxelShapes.empty(),
          (voxelShape, axisAlignedBB) -> {
              final VoxelShape bbShape = VoxelShapes.create(axisAlignedBB);
              return VoxelShapes.combine(voxelShape, bbShape, IBooleanFunction.OR);
          },
          (voxelShape, voxelShape2) -> VoxelShapes.combine(voxelShape, voxelShape2, IBooleanFunction.OR)
        ).simplify();
    }

    private void evictFromCacheIfNeeded() {
        if (cache.size() == 100) {
            cache.remove(cache.keySet().iterator().next());
        }
    }

    private static final class CacheKey {
        private final BoxType type;
        private final List<AxisAlignedBB> bbList;

        private CacheKey(final BoxType type, final List<AxisAlignedBB> bbList) {
            this.type = type;
            this.bbList = bbList;
        }

        public BoxType getType()
        {
            return type;
        }

        public List<AxisAlignedBB> getBbList()
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
