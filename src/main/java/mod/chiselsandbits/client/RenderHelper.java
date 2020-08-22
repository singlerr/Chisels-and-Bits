package mod.chiselsandbits.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;

public class RenderHelper
{

    public static Random RENDER_RANDOM = new Random();

    public static void drawSelectionBoundingBoxIfExists(
      final AxisAlignedBB bb,
      final BlockPos blockPos,
      final PlayerEntity player,
      final float partialTicks,
      final boolean NormalBoundingBox)
    {
        drawSelectionBoundingBoxIfExistsWithColor(bb, blockPos, player, partialTicks, NormalBoundingBox, 0, 0, 0, 102, 32);
    }

    public static void drawSelectionBoundingBoxIfExistsWithColor(
      final AxisAlignedBB bb,
      final BlockPos blockPos,
      final PlayerEntity player,
      final float partialTicks,
      final boolean NormalBoundingBox,
      final int red,
      final int green,
      final int blue,
      final int alpha,
      final int seeThruAlpha)
    {
        if (bb != null)
        {
            final double x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * partialTicks;
            final double y = player.lastTickPosY + (player.getPosY() - player.lastTickPosY) * partialTicks;
            final double z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * partialTicks;

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GL11.glLineWidth(2.0F);
            RenderSystem.disableTexture();
            RenderSystem.depthMask(false);

            if (!NormalBoundingBox)
            {
                RenderHelper.renderBoundingBox(bb.expand(0.002D, 0.002D, 0.002D).offset(-x + blockPos.getX(), -y + blockPos.getY(), -z + blockPos.getZ()), red, green, blue, alpha);
            }

            RenderSystem.disableDepthTest();

            RenderHelper.renderBoundingBox(bb.expand(0.002D, 0.002D, 0.002D).offset(-x + blockPos.getX(), -y + blockPos.getY(), -z + blockPos.getZ()),
              red,
              green,
              blue,
              seeThruAlpha);

            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    public static void drawLineWithColor(
      final Vector3d a,
      final Vector3d b,
      final BlockPos blockPos,
      final PlayerEntity player,
      final float partialTicks,
      final boolean NormalBoundingBox,
      final int red,
      final int green,
      final int blue,
      final int alpha,
      final int seeThruAlpha)
    {
        if (a != null && b != null)
        {
            final double x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * partialTicks;
            final double y = player.lastTickPosY + (player.getPosY() - player.lastTickPosY) * partialTicks;
            final double z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * partialTicks;

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GL11.glLineWidth(2.0F);
            RenderSystem.disableTexture();
            RenderSystem.depthMask(false);
            RenderSystem.shadeModel(GL11.GL_FLAT);

            final Vector3d a2 = a.add(-x + blockPos.getX(), -y + blockPos.getY(), -z + blockPos.getZ());
            final Vector3d b2 = b.add(-x + blockPos.getX(), -y + blockPos.getY(), -z + blockPos.getZ());
            if (!NormalBoundingBox)
            {
                RenderHelper.renderLine(a2, b2, red, green, blue, alpha);
            }

            RenderSystem.disableDepthTest();

            RenderHelper.renderLine(a2, b2, red, green, blue, seeThruAlpha);

            RenderSystem.shadeModel(Minecraft.isAmbientOcclusionEnabled() ? GL11.GL_SMOOTH : GL11.GL_FLAT);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    public static void renderQuads(
      final MatrixStack matrixStack,
      final int alpha,
      final BufferBuilder renderer,
      final List<BakedQuad> quads,
      final World worldObj,
      final BlockPos blockPos,
      int combinedLightIn,
      int combinedOverlayIn)
    {
        int i = 0;
        for (final int j = quads.size(); i < j; ++i)
        {
            final BakedQuad bakedquad = quads.get(i);
            final int color = bakedquad.getTintIndex() == -1 ? alpha | 0xffffff : getTint(alpha, bakedquad.getTintIndex(), worldObj, blockPos);

            float cb = color & 0xFF;
            float cg = (color >>> 8) & 0xFF;
            float cr = (color >>> 16) & 0xFF;
            float ca = (color >>> 24) & 0xFF;

            renderer.addVertexData(matrixStack.getLast(), bakedquad, cb, cg, cr, ca, combinedLightIn, combinedOverlayIn, false);
        }
    }

    // Custom replacement of 1.9.4 -> 1.10's method that changed.
    public static void renderBoundingBox(
      final AxisAlignedBB boundingBox,
      final int red,
      final int green,
      final int blue,
      final int alpha)
    {
        GL11.glPushAttrib(8256);
        final Tessellator tess = Tessellator.getInstance();
        final BufferBuilder buffer = tess.getBuffer();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        final double minX = boundingBox.minX;
        final double minY = boundingBox.minY;
        final double minZ = boundingBox.minZ;
        final double maxX = boundingBox.maxX;
        final double maxY = boundingBox.maxY;
        final double maxZ = boundingBox.maxZ;

        // lower ring ( starts to 0 / 0 )
        buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();

        // Y line at 0 / 0
        buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();

        // upper ring ( including previous point to draw 4 lines )
        buffer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();

        /*
         * the next 3 Y Lines use flat shading to render invisible lines to
         * enable doing this all in one pass.
         */

        // Y line at 1 / 0
        buffer.pos(maxX, minY, minZ).color(red, green, blue, 0).endVertex();
        buffer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();

        // Y line at 0 / 1
        buffer.pos(minX, minY, maxZ).color(red, green, blue, 0).endVertex();
        buffer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();

        // Y line at 1 / 1
        buffer.pos(maxX, minY, maxZ).color(red, green, blue, 0).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();

        tess.draw();
        GL11.glPopAttrib();
    }

    public static void renderLine(
      final Vector3d a,
      final Vector3d b,
      final int red,
      final int green,
      final int blue,
      final int alpha)
    {
        final Tessellator tess = Tessellator.getInstance();
        final BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(a.x, a.y, a.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(b.x, b.y, b.z).color(red, green, blue, alpha).endVertex();
        tess.draw();
    }

    public static int getTint(
      final int alpha,
      final int tintIndex,
      final World worldObj,
      final BlockPos blockPos)
    {
        return alpha | Minecraft.getInstance().getBlockColors().getColor(ChiselsAndBits.getBlocks().getChiseledDefaultState(), worldObj, blockPos, tintIndex);
    }

    public static void renderModel(
      final MatrixStack matrixStack,
      final IBakedModel model,
      final World worldObj,
      final BlockPos blockPos,
      final int alpha,
      final int combinedLightmap,
      final int combinedOverlay)
    {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for (final Direction enumfacing : Direction.values())
        {
            renderQuads(matrixStack, alpha, buffer, model.getQuads(null, enumfacing, RENDER_RANDOM), worldObj, blockPos, combinedLightmap, combinedOverlay);
        }

        renderQuads(matrixStack, alpha, buffer, model.getQuads(null, null, RENDER_RANDOM), worldObj, blockPos, combinedLightmap, combinedOverlay);
        tessellator.draw();
    }

    public static void renderGhostModel(
      final MatrixStack matrixStack,
      final IBakedModel baked,
      final World worldObj,
      final BlockPos blockPos,
      final boolean isUnplaceable,
      final int combinedLightmap,
      final int combinedOverlay)
    {
        final int alpha = isUnplaceable ? 0x22000000 : 0xaa000000;
        RenderSystem.bindTexture(Minecraft.getInstance().getTextureManager().getTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE).getGlTextureId());
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.colorMask(false, false, false, false);

        RenderHelper.renderModel(matrixStack, baked, worldObj, blockPos, alpha, combinedLightmap, combinedOverlay);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderHelper.renderModel(matrixStack, baked, worldObj, blockPos, alpha, combinedLightmap, combinedOverlay);

        RenderSystem.disableBlend();
    }
}
