package mod.chiselsandbits.client.model.baked.chiseled;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.config.IClientConfiguration;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhood;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhoodBuilder;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.profiling.ProfilingManager;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class ChiseledBlockBakedModelManager {
    private static final ChiseledBlockBakedModelManager INSTANCE = new ChiseledBlockBakedModelManager();

    private final SimpleMaxSizedCache<Key, ChiseledBlockBakedModel> cache = new SimpleMaxSizedCache<>(
            () -> IClientConfiguration.getInstance().getModelCacheSize().get() * RenderType.chunkBufferLayers().size()
    );

    private ChiseledBlockBakedModelManager() {
    }

    public static ChiseledBlockBakedModelManager getInstance() {
        return INSTANCE;
    }

    public void clearCache() {
        cache.clear();
    }

    public Optional<ChiseledBlockBakedModel> get(
            final IMultiStateItemStack multiStateItemStack,
            final ChiselRenderType renderType
    ) {
        try (IProfilerSection ignored = ProfilingManager.getInstance().withSection("Item based chiseled block model")) {
            return Optional.of(
                    get(
                            multiStateItemStack,
                            multiStateItemStack.getStatistics().getPrimaryState(),
                            renderType,
                            null,
                            null,
                            BlockPos.ZERO
                    )
            );
        }
    }

    public ChiseledBlockBakedModel get(
            final IAreaAccessor accessor,
            final BlockInformation primaryState,
            final ChiselRenderType renderType
    ) {
        return this.get(accessor, primaryState, renderType, null, null, BlockPos.ZERO);
    }

    public ChiseledBlockBakedModel get(
            final IAreaAccessor accessor,
            final BlockInformation primaryState,
            final ChiselRenderType renderType,
            @Nullable Function<Direction, BlockInformation> neighborhoodBlockInformationProvider,
            @Nullable Function<Direction, IAreaAccessor> neighborhoodAreaAccessorProvider,
            @NotNull BlockPos position
    ) {
        try (IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Block based chiseled block model")) {
            return get(accessor, primaryState, renderType, IBlockNeighborhoodBuilder.getInstance().build(
                    neighborhoodBlockInformationProvider,
                    neighborhoodAreaAccessorProvider
            ), position);
        }
    }

    public ChiseledBlockBakedModel get(
            final IAreaAccessor accessor,
            final BlockInformation primaryState,
            final ChiselRenderType renderType,
            @NotNull IBlockNeighborhood blockNeighborhood,
            @NotNull BlockPos position
    ) {
        try (IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Block based chiseled block model")) {
            final long primaryStateRenderSeed = primaryState.getBlockState().getSeed(position);
            final Key key = new Key(
                    accessor.createNewShapeIdentifier(),
                    primaryState,
                    renderType,
                    blockNeighborhood,
                    primaryStateRenderSeed);
            return cache.get(key,
                    () -> {
                        try (IProfilerSection ignored3 = ProfilingManager.getInstance().withSection("Cache mis")) {
                            return new ChiseledBlockBakedModel(
                                    primaryState,
                                    renderType,
                                    accessor,
                                    targetOffset -> {
                                        final Vec3 nominalTargetOffset = Vec3.ZERO.add(targetOffset);
                                        final BlockPos nominalTargetBlockOffset = new BlockPos(nominalTargetOffset);
                                        final Vec3 inBlockOffset = nominalTargetOffset.subtract(Vec3.atLowerCornerOf(nominalTargetBlockOffset));                                        final Vec3 inBlockOffsetTarget = VectorUtils.makePositive(inBlockOffset);

                                        final Direction offsetDirection = Direction.getNearest(
                                                nominalTargetBlockOffset.getX(),
                                                nominalTargetBlockOffset.getY(),
                                                nominalTargetBlockOffset.getZ()
                                        );

                                        IAreaAccessor neighborAccessor;
                                        if (targetOffset.x() >= 0 && targetOffset.x() < 1 &&
                                                targetOffset.y() >= 0 && targetOffset.y() < 1 &&
                                                targetOffset.z() >= 0 && targetOffset.z() < 1
                                        ) {
                                            neighborAccessor = accessor;
                                        } else {
                                            neighborAccessor = blockNeighborhood.getAreaAccessor(offsetDirection);
                                        }

                                        if (neighborAccessor != null) {
                                            return neighborAccessor.getInAreaTarget(inBlockOffsetTarget)
                                                    .map(IStateEntryInfo::getBlockInformation)
                                                    .orElse(BlockInformation.AIR);
                                        }

                                        return blockNeighborhood.getBlockInformation(offsetDirection);
                                    },
                                    primaryStateRenderSeed
                            );
                        }
                    });
        }
    }

    private static final class Key {
        private final IAreaShapeIdentifier identifier;
        private final BlockInformation primaryState;
        private final ChiselRenderType renderType;
        private final IBlockNeighborhood neighborhood;
        private final long renderSeed;

        private Key(
                final IAreaShapeIdentifier identifier,
                final BlockInformation primaryState,
                final ChiselRenderType renderType,
                final IBlockNeighborhood neighborhood,
                final long renderSeed) {
            this.identifier = identifier;
            this.primaryState = primaryState;
            this.renderType = renderType;
            this.neighborhood = neighborhood;
            this.renderSeed = renderSeed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key key)) return false;

            if (renderSeed != key.renderSeed) return false;
            if (!Objects.equals(identifier, key.identifier)) return false;
            if (!Objects.equals(primaryState, key.primaryState)) return false;
            if (renderType != key.renderType) return false;
            return Objects.equals(neighborhood, key.neighborhood);
        }

        @Override
        public int hashCode() {
            int result = identifier != null ? identifier.hashCode() : 0;
            result = 31 * result + (primaryState != null ? primaryState.hashCode() : 0);
            result = 31 * result + (renderType != null ? renderType.hashCode() : 0);
            result = 31 * result + (neighborhood != null ? neighborhood.hashCode() : 0);
            result = 31 * result + (int) (renderSeed ^ (renderSeed >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "identifier=" + identifier +
                    ", primaryState=" + primaryState +
                    ", renderType=" + renderType +
                    ", neighborhood=" + neighborhood +
                    ", renderSeed=" + renderSeed +
                    '}';
        }
    }
}
