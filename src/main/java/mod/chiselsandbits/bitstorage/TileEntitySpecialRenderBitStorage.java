package mod.chiselsandbits.bitstorage;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.chiseledblock.ChiselRenderType;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBaked;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import mod.chiselsandbits.utils.FluidCuboidHelper;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public class TileEntitySpecialRenderBitStorage extends TileEntityRenderer<TileEntityBitStorage>
{

    private static final SimpleMaxSizedCache<CacheKey, VoxelBlob> STORAGE_CONTENTS_BLOB_CACHE = new SimpleMaxSizedCache<>(ChiselsAndBits.getConfig().getClient().bitStorageContentCacheSize.get());

    public TileEntitySpecialRenderBitStorage(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public void render(
      final TileEntityBitStorage te,
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
                RenderType.getBlockRenderTypes().forEach(renderType -> {
                    if (!RenderTypeLookup.canRenderInLayer(fluidStack.getFluid().getDefaultState(), renderType))
                        return;

                    if (renderType == RenderType.getTranslucent() && Minecraft.isFabulousGraphicsEnabled())
                        renderType = Atlases.getTranslucentCullBlockType();

                    final IVertexBuilder builder = buffer.getBuffer(renderType);

                    final float fullness = (float) fluidStack.getAmount() / (float) TileEntityBitStorage.MAX_CONTENTS;

                    FluidCuboidHelper.renderScaledFluidCuboid(
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
        final BlockState state = te.getMyFluid() == null ? te.getState() : te.getMyFluid().getDefaultState().getBlockState();
        if (bits <= 0 || state == null)
            return;

        VoxelBlob innerModelBlob = STORAGE_CONTENTS_BLOB_CACHE.get(new CacheKey(ModUtil.getStateId(state), bits));
        if (innerModelBlob == null) {
            innerModelBlob = new VoxelBlob();
            innerModelBlob.fillAmountFromBottom(ModUtil.getStateId(state), bits);
            STORAGE_CONTENTS_BLOB_CACHE.put(new CacheKey(ModUtil.getStateId(state), bits), innerModelBlob);
        }

        matrixStackIn.push();
        matrixStackIn.translate(2/16f, 2/16f, 2/16f);
        matrixStackIn.scale(12/16f, 12/16f, 12/16f);
        final VoxelBlob finalInnerModelBlob = innerModelBlob;
        RenderType.getBlockRenderTypes().forEach(renderType -> {
            final ChiseledBlockBaked innerModel = ChiseledBlockSmartModel.getCachedModel(
              ModUtil.getStateId(state),
              new VoxelBlobStateReference(finalInnerModelBlob, 0L),
              ChiselRenderType.fromLayer(renderType, te.getMyFluid() != null),
              DefaultVertexFormats.BLOCK,
              Objects.requireNonNull(te.getWorld()).getRandom()
            );

            if (!innerModel.isEmpty())
            {
                final float r = te.getMyFluid() == null ? 1f : ((te.getMyFluid().getAttributes().getColor() >> 16) & 0xff) / 255F;
                final float g = te.getMyFluid() == null ? 1f : ((te.getMyFluid().getAttributes().getColor() >> 8) & 0xff) / 255f;
                final float b = te.getMyFluid() == null ? 1f : ((te.getMyFluid().getAttributes().getColor()) & 0xff) / 255f;

                Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(matrixStackIn.getLast(), buffer.getBuffer(renderType), state, innerModel, r, g, b, combinedLightIn, combinedOverlayIn,
                  EmptyModelData.INSTANCE);
            }
        });
        matrixStackIn.pop();
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
