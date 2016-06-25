package mod.chiselsandbits.client;

import java.util.List;

import org.lwjgl.opengl.GL11;

import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RenderHelper
{

	public static void drawSelectionBoundingBoxIfExists(
			final AxisAlignedBB bb,
			final BlockPos blockPos,
			final EntityPlayer player,
			final float partialTicks,
			final boolean NormalBoundingBox )
	{
		if ( bb != null )
		{
			final double x = player.lastTickPosX + ( player.posX - player.lastTickPosX ) * partialTicks;
			final double y = player.lastTickPosY + ( player.posY - player.lastTickPosY ) * partialTicks;
			final double z = player.lastTickPosZ + ( player.posZ - player.lastTickPosZ ) * partialTicks;

			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0 );
			GL11.glLineWidth( 2.0F );
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask( false );

			if ( !NormalBoundingBox )
			{
				RenderHelper.renderBoundingBox( bb.expand( 0.002D, 0.002D, 0.002D ).offset( -x + blockPos.getX(), -y + blockPos.getY(), -z + blockPos.getZ() ), 0, 0, 0, 102 );
			}

			GlStateManager.disableDepth();

			RenderHelper.renderBoundingBox( bb.expand( 0.002D, 0.002D, 0.002D ).offset( -x + blockPos.getX(), -y + blockPos.getY(), -z + blockPos.getZ() ), 0, 0, 0, 26 );

			GlStateManager.enableDepth();
			GlStateManager.depthMask( true );
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
		}
	}

	public static void renderQuads(
			final VertexBuffer renderer,
			final List<BakedQuad> quads,
			final World worldObj,
			final BlockPos blockPos )
	{
		int i = 0;
		for ( final int j = quads.size(); i < j; ++i )
		{
			final BakedQuad bakedquad = quads.get( i );
			final int color = bakedquad.getTintIndex() == -1 ? 0xaaffffff : getTint( bakedquad.getTintIndex(), worldObj, blockPos );
			net.minecraftforge.client.model.pipeline.LightUtil.renderQuadColor( renderer, bakedquad, color );
		}
	}

	// Custom replacement of 1.9.4 -> 1.10's method that changed.
	public static void renderBoundingBox(
			final AxisAlignedBB boundingBox,
			final int red,
			final int green,
			final int blue,
			final int alpha )
	{
		final Tessellator tess = Tessellator.getInstance();
		final VertexBuffer buffer = tess.getBuffer();
		buffer.begin( GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR );

		final double minX = boundingBox.minX;
		final double minY = boundingBox.minY;
		final double minZ = boundingBox.minZ;
		final double maxX = boundingBox.maxX;
		final double maxY = boundingBox.maxY;
		final double maxZ = boundingBox.maxZ;

		// lower ring ( starts to 0 / 0 )
		buffer.pos( minX, minY, minZ ).color( red, green, blue, alpha ).endVertex();
		buffer.pos( maxX, minY, minZ ).color( red, green, blue, alpha ).endVertex();
		buffer.pos( maxX, minY, maxZ ).color( red, green, blue, alpha ).endVertex();
		buffer.pos( minX, minY, maxZ ).color( red, green, blue, alpha ).endVertex();
		buffer.pos( minX, minY, minZ ).color( red, green, blue, alpha ).endVertex();

		// Y line at 0 / 0
		buffer.pos( minX, maxY, minZ ).color( red, green, blue, alpha ).endVertex();

		// upper ring ( including previous point to draw 4 lines )
		buffer.pos( maxX, maxY, minZ ).color( red, green, blue, alpha ).endVertex();
		buffer.pos( maxX, maxY, maxZ ).color( red, green, blue, alpha ).endVertex();
		buffer.pos( minX, maxY, maxZ ).color( red, green, blue, alpha ).endVertex();
		buffer.pos( minX, maxY, minZ ).color( red, green, blue, alpha ).endVertex();

		/*
		 * the next 3 Y Lines use flat shading to render invisible lines to
		 * enable doing this all in one pass.
		 */

		// Y line at 1 / 0
		buffer.pos( maxX, minY, minZ ).color( red, green, blue, 0 ).endVertex();
		buffer.pos( maxX, maxY, minZ ).color( red, green, blue, alpha ).endVertex();

		// Y line at 0 / 1
		buffer.pos( minX, minY, maxZ ).color( red, green, blue, 0 ).endVertex();
		buffer.pos( minX, maxY, maxZ ).color( red, green, blue, alpha ).endVertex();

		// Y line at 1 / 1
		buffer.pos( maxX, minY, maxZ ).color( red, green, blue, 0 ).endVertex();
		buffer.pos( maxX, maxY, maxZ ).color( red, green, blue, alpha ).endVertex();

		tess.draw();
	}

	public static int getTint(
			final int tintIndex,
			final World worldObj,
			final BlockPos blockPos )
	{
		return 0xaa000000 | Minecraft.getMinecraft().getBlockColors().colorMultiplier( ChiselsAndBits.getBlocks().getChiseledDefaultState(), worldObj, blockPos, tintIndex );
	}

	public static void renderModel(
			final IBakedModel model,
			final World worldObj,
			final BlockPos blockPos )
	{
		final Tessellator tessellator = Tessellator.getInstance();
		final VertexBuffer worldrenderer = tessellator.getBuffer();
		worldrenderer.begin( 7, DefaultVertexFormats.ITEM );

		for ( final EnumFacing enumfacing : EnumFacing.values() )
		{
			renderQuads( worldrenderer, model.getQuads( null, enumfacing, 0 ), worldObj, blockPos );
		}

		renderQuads( worldrenderer, model.getQuads( null, null, 0 ), worldObj, blockPos );
		tessellator.draw();
	}

	public static void renderGhostModel(
			final IBakedModel baked,
			final World worldObj,
			final BlockPos blockPos )
	{
		GlStateManager.bindTexture( Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId() );
		GlStateManager.color( 1.0f, 1.0f, 1.0f, 0.1f );
		GlStateManager.enableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.blendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
		GlStateManager.colorMask( false, false, false, false );

		RenderHelper.renderModel( baked, worldObj, blockPos );
		GlStateManager.colorMask( true, true, true, true );
		GlStateManager.depthFunc( GL11.GL_LEQUAL );
		RenderHelper.renderModel( baked, worldObj, blockPos );

		GlStateManager.color( 1.0f, 1.0f, 1.0f, 1.0f );
		GlStateManager.disableBlend();
	}

}
