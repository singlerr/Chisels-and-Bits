package mod.chiselsandbits.client.besr;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.config.IClientConfiguration;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.block.entities.storage.SimpleStateEntryStorage;
import mod.chiselsandbits.client.model.baked.chiseled.ChiselRenderType;
import mod.chiselsandbits.client.model.baked.chiseled.ChiseledBlockBakedModel;
import mod.chiselsandbits.client.model.baked.chiseled.ChiseledBlockBakedModelManager;
import mod.chiselsandbits.client.util.FluidCuboidUtils;
import mod.chiselsandbits.platforms.core.client.rendering.IRenderingManager;
import mod.chiselsandbits.platforms.core.client.rendering.type.IRenderTypeManager;
import mod.chiselsandbits.platforms.core.fluid.FluidInformation;
import mod.chiselsandbits.platforms.core.fluid.IFluidManager;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BitStorageBESR implements BlockEntityRenderer<BitStorageBlockEntity>
{
    private static final SimpleMaxSizedCache<CacheKey, IStateEntryStorage> STORAGE_CONTENTS_BLOB_CACHE = new SimpleMaxSizedCache<>(IClientConfiguration.getInstance().getBitStorageContentCacheSize()::get);

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
        final BlockInformation blockInformation = te.getContainedBlockInformation();
        if (bits <= 0 || blockInformation == null)
            return;

        final CacheKey cacheKey = new CacheKey(blockInformation, bits);
        IStateEntryStorage innerModelBlob = STORAGE_CONTENTS_BLOB_CACHE.get(cacheKey);
        if (innerModelBlob == null) {
            innerModelBlob = new SimpleStateEntryStorage();

            innerModelBlob.fillFromBottom(
              blockInformation,
              bits
            );
            STORAGE_CONTENTS_BLOB_CACHE.put(cacheKey, innerModelBlob);
        }

        poseStack.pushPose();
        poseStack.translate(2/16f, 2/16f, 2/16f);
        poseStack.scale(12/16f, 12/16f, 12/16f);
        final IStateEntryStorage finalInnerModelBlob = innerModelBlob;
        RenderType.chunkBufferLayers().forEach(renderType -> {
            final ChiseledBlockBakedModel innerModel = ChiseledBlockBakedModelManager.getInstance().get(
              MultiStateSnapshotUtils.createFromStorage(finalInnerModelBlob),
              blockInformation,
              ChiselRenderType.fromLayer(renderType, te.containsFluid()),
              null,
              null,
              te.getBlockPos()
            );

            if (!innerModel.isEmpty())
            {
                float r;
                float g;
                float b;
                if (te.containsFluid()) {
                    r = te.getFluid().map(IFluidManager.getInstance()::getFluidColor).map(color -> (color >> 16) & 0xff).orElse(255) / 255f;
                    g = te.getFluid().map(IFluidManager.getInstance()::getFluidColor).map(color -> (color >> 8) & 0xff).orElse(255) / 255f;
                    b = te.getFluid().map(IFluidManager.getInstance()::getFluidColor).map(color -> color & 0xff).orElse(255) / 255f;
                } else {
                    final int color = Minecraft.getInstance().getBlockColors().getColor(blockInformation.getBlockState(), te.getLevel(), te.getBlockPos());
                    r = ((color >> 16) & 0xff) / 255f;
                    g = ((color >> 8) & 0xff) / 255f;
                    b = (color  & 0xff) / 255f;
                }


                IRenderingManager.getInstance().renderModel(poseStack.last(), buffer.getBuffer(renderType), blockInformation.getBlockState(), innerModel, r, g, b, combinedLightIn, combinedOverlayIn);
            }
        });
        poseStack.popPose();
    }

    private static final class CacheKey {
        private final BlockInformation blockInformation;
        private final int              bitCount;

        private CacheKey(final BlockInformation blockInformation, final int bitCount) {
            this.blockInformation = blockInformation;
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
            return blockInformation == cacheKey.blockInformation &&
                     bitCount == cacheKey.bitCount;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(blockInformation, bitCount);
        }
    }
}
