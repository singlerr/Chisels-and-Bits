package mod.chiselsandbits.client.besr;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.config.IClientConfiguration;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.client.model.baked.chiseled.ChiselRenderType;
import mod.chiselsandbits.client.model.baked.chiseled.ChiseledBlockBakedModel;
import mod.chiselsandbits.client.model.baked.chiseled.ChiseledBlockBakedModelManager;
import mod.chiselsandbits.client.util.FluidCuboidUtils;
import mod.chiselsandbits.platforms.core.client.rendering.IRenderingManager;
import mod.chiselsandbits.platforms.core.client.rendering.type.IRenderTypeManager;
import mod.chiselsandbits.platforms.core.fluid.FluidInformation;
import mod.chiselsandbits.platforms.core.fluid.IFluidManager;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BitStorageBESR implements BlockEntityRenderer<BitStorageBlockEntity>
{
    private static final SimpleMaxSizedCache<CacheKey, LevelChunkSection> STORAGE_CONTENTS_BLOB_CACHE = new SimpleMaxSizedCache<>(IClientConfiguration.getInstance().getBitStorageContentCacheSize()::get);

    public static void clearCache() {
        STORAGE_CONTENTS_BLOB_CACHE.clear();
    }

    public BitStorageBESR()
    {
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void render(
      final BitStorageBlockEntity te,
      final float partialTicks,
      final @NotNull PoseStack poseStack,
      final @NotNull MultiBufferSource buffer,
      final int combinedLightIn,
      final int combinedOverlayIn)
    {
        if (te.containsFluid()) {
            final FluidInformation fluidStack = te.getFluid().get();
            if (fluidStack != null)
            {
                RenderType.chunkBufferLayers().forEach(renderType -> {
                    if (!IRenderTypeManager.getInstance().canRenderInType(fluidStack.fluid().defaultFluidState(), renderType))
                        return;

                    if (renderType == RenderType.translucent() && Minecraft.useShaderTransparency())
                        renderType = Sheets.translucentCullBlockSheet();

                    final VertexConsumer builder = buffer.getBuffer(renderType);

                    final float fullness = (float) fluidStack.amount() / (float) StateEntrySize.current().getBitsPerBlock();

                    FluidCuboidUtils.renderScaledFluidCuboid(
                      fluidStack,
                      poseStack,
                      builder,
                      combinedLightIn,
                      combinedOverlayIn,
                      1, 1, 1,
                      15, 15 * fullness, 15
                    );
                });
            }

            return;
        }

        final int bits = te.getBits();
        final BlockState state = te.getState();
        if (bits <= 0 || state == null)
            return;

        final CacheKey cacheKey = new CacheKey(IBlockStateIdManager.getInstance().getIdFrom(state), bits);
        LevelChunkSection innerModelBlob = STORAGE_CONTENTS_BLOB_CACHE.get(cacheKey);
        if (innerModelBlob == null) {
            innerModelBlob = new LevelChunkSection(1);
            ChunkSectionUtils.fillFromBottom(
              innerModelBlob,
              state,
              bits
            );
            STORAGE_CONTENTS_BLOB_CACHE.put(cacheKey, innerModelBlob);
        }

        poseStack.pushPose();
        poseStack.translate(2/16f, 2/16f, 2/16f);
        poseStack.scale(12/16f, 12/16f, 12/16f);
        final LevelChunkSection finalInnerModelBlob = innerModelBlob;
        RenderType.chunkBufferLayers().forEach(renderType -> {
            final ChiseledBlockBakedModel innerModel = ChiseledBlockBakedModelManager.getInstance().get(
              MultiStateSnapshotUtils.createFromSection(finalInnerModelBlob),
              state,
              ChiselRenderType.fromLayer(renderType, te.containsFluid()),
              null,
              null,
              te.getBlockPos()
            );

            if (!innerModel.isEmpty())
            {
                final float r = te.getFluid().map(IFluidManager.getInstance()::getFluidColor).map(color -> (color >> 16) & 0xff).orElse(255) / 255f;
                final float g = te.getFluid().map(IFluidManager.getInstance()::getFluidColor).map(color -> (color >> 8) & 0xff).orElse(255) / 255f;
                final float b = te.getFluid().map(IFluidManager.getInstance()::getFluidColor).map(color -> color & 0xff).orElse(255) / 255f;

                IRenderingManager.getInstance().renderModel(poseStack.last(), buffer.getBuffer(renderType), state, innerModel, r, g, b, combinedLightIn, combinedOverlayIn);
            }
        });
        poseStack.popPose();
    }

    private static final class CacheKey {
        private final int blockStateId;
        private final int bitCount;

        private CacheKey(final int blockStateId, final int bitCount) {
            this.blockStateId = blockStateId;
            this.bitCount = bitCount;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof final CacheKey cacheKey))
            {
                return false;
            }
            return blockStateId == cacheKey.blockStateId &&
                     bitCount == cacheKey.bitCount;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(blockStateId, bitCount);
        }
    }
}
