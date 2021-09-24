package mod.chiselsandbits.client.model.baked.chiseled;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.profiling.ProfilingManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;
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
        .maximumSize(Configuration.getInstance().getClient().modelCacheSize.get() * RenderType.getBlockRenderTypes().size())
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build();

    private ChiseledBlockBakedModelManager()
    {
    }

    public ChiseledBlockBakedModel get(
      final IAreaAccessor accessor,
      final BlockState primaryState,
      final ChiselRenderType renderType,
      @Nullable IBlockReader blockReader,
      @Nullable BlockPos position
    ) {
        try(IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Block based chiseled block model")) {
            final EnumMap<Direction, BlockState> neighborhoodMap = new EnumMap<>(Direction.class);

            try(IProfilerSection ignored2 = ProfilingManager.getInstance().withSection("Key building")) {
                if (blockReader != null && position != null) {
                    for (final Direction value : Direction.values())
                    {
                        final BlockPos offsetPos = position.offset(value);
                        neighborhoodMap.put(value, blockReader.getBlockState(offsetPos));
                    }
                }
            }

            final long primaryStateRenderSeed = primaryState.getPositionRandom(position);
            final Key key = new Key(accessor.createNewShapeIdentifier(), primaryState, renderType, neighborhoodMap, primaryStateRenderSeed);
            try
            {
                return cache.get(key,
                  () -> {
                      try (IProfilerSection ignored3 = ProfilingManager.getInstance().withSection("Cache mis")) {
                          return new ChiseledBlockBakedModel(
                            primaryState,
                            renderType,
                            accessor,
                            targetOffset -> {
                                if (blockReader == null || position == null)
                                {
                                    return Blocks.AIR.getDefaultState();
                                }

                                final Vector3d targetPositionVector = Vector3d.copy(position).add(targetOffset);
                                final BlockPos targetPosition = new BlockPos(targetPositionVector);

                                final TileEntity tileEntity = blockReader.getTileEntity(targetPosition);
                                if (tileEntity instanceof IMultiStateBlockEntity)
                                {
                                    final IMultiStateBlockEntity blockEntity = (IMultiStateBlockEntity) tileEntity;

                                    final Vector3d inBlockOffset = targetPositionVector.subtract(Vector3d.copy(targetPosition));
                                    final Vector3d inBlockOffsetTarget = VectorUtils.makePositive(inBlockOffset);

                                    return blockEntity.getInAreaTarget(inBlockOffsetTarget)
                                             .map(IStateEntryInfo::getState)
                                             .orElse(Blocks.AIR.getDefaultState());
                                }

                                return blockReader.getBlockState(targetPosition);
                            },
                            primaryStateRenderSeed
                          );
                      }
                  });
            }
            catch (ExecutionException e)
            {
                LOGGER.error("Failed to calculate the chiseled block model. Calculation was interrupted.", e);
                return ChiseledBlockBakedModel.EMPTY;
            }
        }
    }

    public Optional<ChiseledBlockBakedModel> get(
      final IMultiStateItemStack multiStateItemStack,
      final ChiselRenderType renderType
    ) {
        try(IProfilerSection ignored = ProfilingManager.getInstance().withSection("Item based chiseled block model"))
        {
            return Optional.of(
              get(
                multiStateItemStack,
                multiStateItemStack.getStatistics().getPrimaryState(),
                renderType,
                null,
                BlockPos.ZERO
              )
            );
        }
    }

    private static final class Key {
        private final IAreaShapeIdentifier identifier;
        private final BlockState primaryState;
        private final ChiselRenderType renderType;
        private final EnumMap<Direction, BlockState> neighborhoodMap;
        private final long renderSeed;

        private Key(
          final IAreaShapeIdentifier identifier,
          final BlockState primaryState,
          final ChiselRenderType renderType,
          final EnumMap<Direction, BlockState> neighborhoodMap, final long renderSeed) {
            this.identifier = identifier;
            this.primaryState = primaryState;
            this.renderType = renderType;
            this.neighborhoodMap = neighborhoodMap;
            this.renderSeed = renderSeed;
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

            if (renderSeed != key.renderSeed)
            {
                return false;
            }
            if (!Objects.equals(identifier, key.identifier))
            {
                return false;
            }
            if (!Objects.equals(primaryState, key.primaryState))
            {
                return false;
            }
            if (renderType != key.renderType)
            {
                return false;
            }
            return Objects.equals(neighborhoodMap, key.neighborhoodMap);
        }

        @Override
        public int hashCode()
        {
            int result = identifier != null ? identifier.hashCode() : 0;
            result = 31 * result + (primaryState != null ? primaryState.hashCode() : 0);
            result = 31 * result + (renderType != null ? renderType.hashCode() : 0);
            result = 31 * result + (neighborhoodMap != null ? neighborhoodMap.hashCode() : 0);
            result = 31 * result + (int) (renderSeed ^ (renderSeed >>> 32));
            return result;
        }
    }
}
