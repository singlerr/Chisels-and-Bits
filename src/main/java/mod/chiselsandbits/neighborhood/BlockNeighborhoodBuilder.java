package mod.chiselsandbits.neighborhood;

import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhood;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhoodBuilder;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.profiling.ProfilingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.function.Function;

public final class BlockNeighborhoodBuilder implements IBlockNeighborhoodBuilder {
    private static final BlockNeighborhoodBuilder INSTANCE = new BlockNeighborhoodBuilder();

    public static BlockNeighborhoodBuilder getInstance() {
        return INSTANCE;
    }

    private BlockNeighborhoodBuilder() {
    }

    @Override
    public @NotNull IBlockNeighborhood build(
            @Nullable Function<Direction, BlockState> neighborhoodBlockStateProvider,
            @Nullable Function<Direction, IAreaAccessor> neighborhoodAreaAccessorProvider
    ) {
        final EnumMap<Direction, BlockNeighborhoodEntry> neighborhoodMap = new EnumMap<>(Direction.class);

        try (IProfilerSection ignored2 = ProfilingManager.getInstance().withSection("Key building")) {
            if (neighborhoodBlockStateProvider != null && neighborhoodAreaAccessorProvider != null) {
                for (final Direction value : Direction.values()) {
                    final BlockState state = neighborhoodBlockStateProvider.apply(value);
                    final IAreaAccessor accessor = neighborhoodAreaAccessorProvider.apply(value);
                    if (accessor == null) {
                        neighborhoodMap.put(value, new BlockNeighborhoodEntry(state));
                    } else {
                        neighborhoodMap.put(value, new BlockNeighborhoodEntry(
                                        state,
                                        accessor.createNewShapeIdentifier()
                                )
                        );
                    }
                }
            }
        }

        return new BlockNeighborhood(neighborhoodMap);
    }
}
