package mod.chiselsandbits.api.map.bit;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.DepthProjection;
import mod.chiselsandbits.api.util.Vector2i;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class BitDepthMapBuilder implements IDepthMapBuilder {

    public static BitDepthMapBuilder create(IAreaAccessor accessor, DepthProjection projection) {
        return new BitDepthMapBuilder(accessor, projection);
    }

    private final IAreaAccessor accessor;
    private final DepthProjection projection;

    private BitDepthMapBuilder(IAreaAccessor accessor, DepthProjection projection) {
        this.accessor = accessor;
        this.projection = projection;
    }

    public IBitDepthMap build() {
        return new BitDepthMap(this);
    }

    @Override
    public Vec3i getSize() {
        final AABB bitSizedBoundingBox = this.accessor.getBitScaledBoundingBox();

        return switch (projection.getDepthDirection().getAxis()) {
            case X -> new Vec3i(
                    bitSizedBoundingBox.getZsize(),
                    bitSizedBoundingBox.getYsize(),
                    bitSizedBoundingBox.getXsize()
            );
            case Y -> new Vec3i(
                    bitSizedBoundingBox.getXsize(),
                    bitSizedBoundingBox.getZsize(),
                    bitSizedBoundingBox.getYsize()
            );
            case Z -> new Vec3i(
                    bitSizedBoundingBox.getXsize(),
                    bitSizedBoundingBox.getYsize(),
                    bitSizedBoundingBox.getZsize()
            );
        };
    }

    @Override
    public Stream<IDepthMapEntry> getEntries() {
        record StateEntryInfoWithDepth(IStateEntryInfo stateEntryInfo, Vec3i originalPosition, int depth) {}

        final AABB bitBox = this.accessor.getBitScaledBoundingBox();
        final Multimap<Vector2i, StateEntryInfoWithDepth> groupedEntries = ArrayListMultimap.create();

        projection.forEach((int) bitBox.getXsize(), (int) bitBox.getYsize(), (int) bitBox.getZsize(), (x, y, z, h, v, d, hSize, vSize, dSize) -> {
            final Vec3 inAccessorPosition = new Vec3(x, y, z).multiply(StateEntrySize.current().getSizePerBitScalingVector()).add(accessor.getLocalizedOffset());
            final Optional<IStateEntryInfo> stateEntryInfo = accessor.getInAreaTarget(inAccessorPosition);
            if (stateEntryInfo.isEmpty()) {
                return;
            }

            if (stateEntryInfo.get().getBlockInformation().isAir()) {
                return;
            }

            final int workingDepth = projection.getDepthDirection().getAxisDirection() == Direction.AxisDirection.NEGATIVE ? d : dSize - d;

            groupedEntries.put(new Vector2i(h, v), new StateEntryInfoWithDepth(stateEntryInfo.get(), new Vec3i(x, y, z), workingDepth));
        });

        final Map<Vector2i, IDepthMapEntry> results = new HashMap<>();

        groupedEntries.asMap().entrySet().stream().map(entry -> {
            final Vector2i position = entry.getKey();
            final List<StateEntryInfoWithDepth> stateEntryInfos = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(StateEntryInfoWithDepth::depth).reversed()).toList();

            if (stateEntryInfos.isEmpty()) {
                return new DepthMapEntry(position, 0);
            }
            final StateEntryInfoWithDepth first = stateEntryInfos.get(0);

            return new DepthMapEntry(position, first.depth());
        }).forEach(entry -> results.put(entry.depthMapPosition(), entry));

        return results.values().stream();
    }

    public record Entry(Vector2i depthMapPosition, Vec3i originalPosition, double depth) implements IDepthMapEntry {}
}
