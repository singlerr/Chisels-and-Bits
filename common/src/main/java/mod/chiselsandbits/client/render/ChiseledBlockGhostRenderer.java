package mod.chiselsandbits.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ChiseledBlockGhostRenderer
{
    private static final ChiseledBlockGhostRenderer INSTANCE = new ChiseledBlockGhostRenderer();

    private static final BufferBuilderTransparent BUFFER = new BufferBuilderTransparent();

    public static ChiseledBlockGhostRenderer getInstance()
    {
        return INSTANCE;
    }

    private ChiseledBlockGhostRenderer()
    {
    }

    public void renderGhost(
            final PoseStack poseStack,
            final ItemStack renderStack,
            final Vec3 targetedRenderPos,
            final Vector4f color,
            final boolean ignoreDepth)
    {
        poseStack.pushPose();

        // Offset/scale by an unnoticeable amount to prevent z-fighting
        final Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.translate(
          targetedRenderPos.x - camera.x - 0.000125,
          targetedRenderPos.y - camera.y + 0.000125,
          targetedRenderPos.z - camera.z - 0.000125
        );
        poseStack.scale(1.001F, 1.001F, 1.001F);

        BUFFER.setAlphaPercentage(color.w());
        final BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(renderStack, null, null, 0);
        renderGhost(poseStack, renderStack, model, false);
        if (ignoreDepth)
            renderGhost(poseStack, renderStack, model, true);

        poseStack.popPose();
    }

    private void renderGhost(
            final PoseStack poseStack,
            final ItemStack renderStack,
            final BakedModel model,
            final boolean ignoreDepth)
    {
        final RenderType renderType = ignoreDepth
                ? ModRenderTypes.GHOST_BLOCK_PREVIEW_GREATER.get()
                : ModRenderTypes.GHOST_BLOCK_PREVIEW.get();

        BUFFER.begin(renderType.mode(), renderType.format());
        Minecraft.getInstance().getItemRenderer().renderModelLists(
          model,
          renderStack,
          15728880,
          OverlayTexture.NO_OVERLAY,
          poseStack,
          BUFFER
        );
        renderType.end(BUFFER, 0, 0, 0);
    }

    private static class BufferBuilderTransparent extends BufferBuilder
    {
        private float alphaPercentage;

        public BufferBuilderTransparent()
        {
            super(2097152);
        }

        public void setAlphaPercentage(final float alphaPercentage)
        {
            this.alphaPercentage = Mth.clamp(alphaPercentage, 0, 1);
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha)
        {
            return super.color(red, green, blue, alpha * alphaPercentage);
        }

        @Override
        public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU,
                           float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ)
        {
            super.vertex(x, y, z, red, green, blue, alpha * alphaPercentage, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
        }
    }
}
