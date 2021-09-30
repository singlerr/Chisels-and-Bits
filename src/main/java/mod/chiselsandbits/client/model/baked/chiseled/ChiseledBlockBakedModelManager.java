package mod.chiselsandbits.client.model.baked.chiseled;

import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhood;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhoodBuilder;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.neighborhood.BlockNeighborhoodEntry;
import mod.chiselsandbits.profiling.ProfilingManager;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;

public class ChiseledBlockBakedModelManager
{
    private static final ChiseledBlockBakedModelManager INSTANCE = new ChiseledBlockBakedModelManager();

    private final SimpleMaxSizedCache<Key, ChiseledBlockBakedModel> cache = new SimpleMaxSizedCache<>(
      () -> Configuration.getInstance().getClient().modelCacheSize.get() * RenderType.chunkBufferLayers().size()
    );

    private ChiseledBlockBakedModelManager()
    {
    }

    public static ChiseledBlockBakedModelManager getInstance()
    {
        return INSTANCE;
    }

    public void clearCache() {
        cache.clear();
    }

    public Optional<ChiseledBlockBakedModel> get(
      final IMultiStateItemStack multiStateItemStack,
      final ChiselRenderType renderType
    )
    {
        try (IProfilerSection ignored = ProfilingManager.getInstance().withSection("Item based chiseled block model"))
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

    public ChiseledBlockBakedModel get(
      final IAreaAccessor accessor,
      final BlockState primaryState,
      final ChiselRenderType renderType
    ) {
        return this.get(accessor, primaryState, renderType, null, BlockPos.ZERO);
    }

    public ChiseledBlockBakedModel get(
      final IAreaAccessor accessor,
      final BlockState primaryState,
      final ChiselRenderType renderType,
      @Nullable IBlockReader blockReader,
      @NotNull BlockPos position
    )
    {
        try (IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Block based chiseled block model"))
        {
            final long primaryStateRenderSeed = primaryState.getSeed(position);
            final Key key = new Key(
              accessor.createNewShapeIdentifier(),
              primaryState,
              renderType,
              IBlockNeighborhoodBuilder.getInstance().build(blockReader, position),
              primaryStateRenderSeed);
            return cache.get(key,
              () -> {
                  try (IProfilerSection ignored3 = ProfilingManager.getInstance().withSection("Cache mis"))
                  {
                      return new ChiseledBlockBakedModel(
                        primaryState,
                        renderType,
                        accessor,
                        targetOffset -> {
                            if (blockReader == null || position == null)
                            {
                                return Blocks.AIR.defaultBlockState();
                            }

                            final Vector3d targetPositionVector = Vector3d.atLowerCornerOf(position).add(targetOffset);
                            final BlockPos targetPosition = new BlockPos(targetPositionVector);

                            final TileEntity tileEntity = blockReader.getBlockEntity(targetPosition);
                            if (tileEntity instanceof IMultiStateBlockEntity)
                            {
                                final IMultiStateBlockEntity blockEntity = (IMultiStateBlockEntity) tileEntity;

                                final Vector3d inBlockOffset = targetPositionVector.subtract(Vector3d.atLowerCornerOf(targetPosition));
                                final Vector3d inBlockOffsetTarget = VectorUtils.makePositive(inBlockOffset);

                                return blockEntity.getInAreaTarget(inBlockOffsetTarget)
                                  .map(IStateEntryInfo::getState)
                                  .orElse(Blocks.AIR.defaultBlockState());
                            }

                            return blockReader.getBlockState(targetPosition);
                        },
                        primaryStateRenderSeed
                      );
                  }
              });
        }
    }

    private static final class Key
    {
        private final IAreaShapeIdentifier                 identifier;
        private final BlockState                           primaryState;
        private final ChiselRenderType                           renderType;
        private final IBlockNeighborhood                         neighborhood;
        private final long                                       renderSeed;

        private Key(
          final IAreaShapeIdentifier identifier,
          final BlockState primaryState,
          final ChiselRenderType renderType,
          final IBlockNeighborhood neighborhood,
          final long renderSeed)
        {
            this.identifier = identifier;
            this.primaryState = primaryState;
            this.renderType = renderType;
            this.neighborhood = neighborhood;
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
            return Objects.equals(neighborhood, key.neighborhood);
        }

        @Override
        public int hashCode()
        {
            int result = identifier != null ? identifier.hashCode() : 0;
            result = 31 * result + (primaryState != null ? primaryState.hashCode() : 0);
            result = 31 * result + (renderType != null ? renderType.hashCode() : 0);
            result = 31 * result + (neighborhood != null ? neighborhood.hashCode() : 0);
            result = 31 * result + (int) (renderSeed ^ (renderSeed >>> 32));
            return result;
        }
    }
}
