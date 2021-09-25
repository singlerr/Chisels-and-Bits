package mod.chiselsandbits.client.besr;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.client.model.baked.chiseled.ChiselRenderType;
import mod.chiselsandbits.client.model.baked.chiseled.ChiseledBlockBakedModel;
import mod.chiselsandbits.client.model.baked.chiseled.ChiseledBlockBakedModelManager;
import mod.chiselsandbits.client.util.FluidCuboidUtils;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public class BitStorageBESR extends TileEntityRenderer<BitStorageBlockEntity>
{

    private static final SimpleMaxSizedCache<CacheKey, ChunkSection> STORAGE_CONTENTS_BLOB_CACHE = new SimpleMaxSizedCache<>(Configuration.getInstance().getClient().bitStorageContentCacheSize.get());

    public BitStorageBESR(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public void render(
      final BitStorageBlockEntity te,
      final float partialTicks,
      final MatrixStack matrixStackIn,
      final IRenderTypeBuffer buffer,
      final int combinedLightIn,
      final int combinedOverlayIn)
    {
        if (te.getMyFluid() != null) {
            final FluidStack fluidStack = te.getBitsAsFluidStack();
            if (fluidStack != null)
            {
                RenderType.chunkBufferLayers().forEach(renderType -> {
                    if (!RenderTypeLookup.canRenderInLayer(fluidStack.getFluid().defaultFluidState(), renderType))
                        return;

                    if (renderType == RenderType.translucent() && Minecraft.useShaderTransparency())
                        renderType = Atlases.translucentCullBlockSheet();

                    final IVertexBuilder builder = buffer.getBuffer(renderType);

                    final float fullness = (float) fluidStack.getAmount() / (float) BitStorageBlockEntity.MAX_CONTENTS;

                    FluidCuboidUtils.renderScaledFluidCuboid(
                      fluidStack,
                      matrixStackIn,
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
        final BlockState state = te.getMyFluid() == null ? te.getState() : te.getMyFluid().defaultFluidState().createLegacyBlock();
        if (bits <= 0 || state == null)
            return;

        final CacheKey cacheKey = new CacheKey(IBlockStateIdManager.getInstance().getIdFrom(state), bits);
        ChunkSection innerModelBlob = STORAGE_CONTENTS_BLOB_CACHE.get(cacheKey);
        if (innerModelBlob == null) {
            innerModelBlob = new ChunkSection(1);
            ChunkSectionUtils.fillFromBottom(
              innerModelBlob,
              state,
              bits
            );
            STORAGE_CONTENTS_BLOB_CACHE.put(cacheKey, innerModelBlob);
        }

        matrixStackIn.pushPose();
        matrixStackIn.translate(2/16f, 2/16f, 2/16f);
        matrixStackIn.scale(12/16f, 12/16f, 12/16f);
        final ChunkSection finalInnerModelBlob = innerModelBlob;
        RenderType.chunkBufferLayers().forEach(renderType -> {
            final ChiseledBlockBakedModel innerModel = ChiseledBlockBakedModelManager.getInstance().get(
              MultiStateSnapshotUtils.createFromSection(finalInnerModelBlob),
              state,
              ChiselRenderType.fromLayer(renderType, te.getMyFluid() != null),
              new SingleBlockBlockReader(
                state,
                te.getBlockPos(),
                te.getLevel()
              ),
              te.getBlockPos()
            );

            if (!innerModel.isEmpty())
            {
                final float r = te.getMyFluid() == null ? 1f : ((te.getMyFluid().getAttributes().getColor() >> 16) & 0xff) / 255F;
                final float g = te.getMyFluid() == null ? 1f : ((te.getMyFluid().getAttributes().getColor() >> 8) & 0xff) / 255f;
                final float b = te.getMyFluid() == null ? 1f : ((te.getMyFluid().getAttributes().getColor()) & 0xff) / 255f;

                Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStackIn.last(), buffer.getBuffer(renderType), state, innerModel, r, g, b, combinedLightIn, combinedOverlayIn,
                  EmptyModelData.INSTANCE);
            }
        });
        matrixStackIn.popPose();
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
            if (!(o instanceof CacheKey))
            {
                return false;
            }
            final CacheKey cacheKey = (CacheKey) o;
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
