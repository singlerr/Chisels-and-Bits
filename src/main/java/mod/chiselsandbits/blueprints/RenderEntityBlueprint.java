package mod.chiselsandbits.blueprints;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import mod.chiselsandbits.blueprints.BlueprintData.EnumLoadState;
import mod.chiselsandbits.client.RenderHelper;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

public class RenderEntityBlueprint extends Render<EntityBlueprint>
{

	EntityItem ei;
	RenderEntityItem rei;
	FloatBuffer orientationMatrix = BufferUtils.createFloatBuffer( 16 );

	public RenderEntityBlueprint(
			final RenderManager renderManager )
	{
		super( renderManager );
		rei = new RenderEntityItem( renderManager, Minecraft.getMinecraft().getRenderItem() );
		ei = new EntityItem( null );
	}

	@Override
	public void doRender(
			final EntityBlueprint entity,
			final double x,
			final double y,
			final double z,
			final float entityYaw,
			final float partialTicks )
	{
		final EnumFacing axisX = entity.getDataManager().get( EntityBlueprint.BLUEPRINT_AXIS_X );
		final EnumFacing axisY = entity.getDataManager().get( EntityBlueprint.BLUEPRINT_AXIS_Y );
		final EnumFacing axisZ = entity.getDataManager().get( EntityBlueprint.BLUEPRINT_AXIS_Z );

		final int minX = entity.getDataManager().get( EntityBlueprint.BLUEPRINT_MIN_X );
		final int minY = entity.getDataManager().get( EntityBlueprint.BLUEPRINT_MIN_Y );
		final int minZ = entity.getDataManager().get( EntityBlueprint.BLUEPRINT_MIN_Z );
		final int maxX = entity.getDataManager().get( EntityBlueprint.BLUEPRINT_MAX_X );
		final int maxY = entity.getDataManager().get( EntityBlueprint.BLUEPRINT_MAX_Y );
		final int maxZ = entity.getDataManager().get( EntityBlueprint.BLUEPRINT_MAX_Z );

		ei.setWorld( entity.getEntityWorld() );
		ei.posX = entity.posX;
		ei.posY = entity.posY;
		ei.posZ = entity.posZ;
		ei.lastTickPosX = entity.lastTickPosX;
		ei.lastTickPosY = entity.lastTickPosY;
		ei.lastTickPosZ = entity.lastTickPosZ;
		ei.setEntityItemStack( entity.getItemStack() );
		ei.setAgeToCreativeDespawnTime();
		rei.doRender( ei, x, y - 0.3, z, entityYaw, partialTicks + entity.getRotation() );

		if ( entity.renderData != null )
		{
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F );

			final double intToPos = 1.0 / 16.0;
			GlStateManager.pushMatrix();
			GlStateManager.translate( x - 0.5 - minX * intToPos, y - 0.5 - minY * intToPos, z - 0.5 - minZ * intToPos );
			GlStateManager.color( 1, 1, 1, 1 );
			net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();

			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();
			GlStateManager.disableTexture2D();

			adjustAxis( axisX, minX + maxX, minY + maxY, minZ + maxZ );
			adjustAxis( axisY, minX + maxX, minY + maxY, minZ + maxZ );
			adjustAxis( axisZ, minX + maxX, minY + maxY, minZ + maxZ );

			orientationMatrix.clear();
			orientationMatrix.put( axisX.getFrontOffsetX() );
			orientationMatrix.put( axisX.getFrontOffsetY() );
			orientationMatrix.put( axisX.getFrontOffsetZ() );
			orientationMatrix.put( 0.0F );

			orientationMatrix.put( axisY.getFrontOffsetX() );
			orientationMatrix.put( axisY.getFrontOffsetY() );
			orientationMatrix.put( axisY.getFrontOffsetZ() );
			orientationMatrix.put( 0.0F );

			orientationMatrix.put( axisZ.getFrontOffsetX() );
			orientationMatrix.put( axisZ.getFrontOffsetY() );
			orientationMatrix.put( axisZ.getFrontOffsetZ() );
			orientationMatrix.put( 0.0F );

			orientationMatrix.put( 0.0F );
			orientationMatrix.put( 0.0F );
			orientationMatrix.put( 0.0F );
			orientationMatrix.put( 1.0F );
			orientationMatrix.rewind();

			GlStateManager.multMatrix( orientationMatrix );

			GlStateManager.colorMask( false, false, false, false );
			( (BlueprintRenderData) entity.renderData ).render();
			GlStateManager.colorMask( true, true, true, true );

			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.enableTexture2D();
			GlStateManager.blendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
			GlStateManager.bindTexture( Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId() );

			( (BlueprintRenderData) entity.renderData ).render();

			GlStateManager.disableBlend();

			net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0 );
		GL11.glLineWidth( 2.0F );
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask( false );
		GlStateManager.shadeModel( GL11.GL_FLAT );

		final AxisAlignedBB bb = entity.getBox();
		RenderHelper.renderBoundingBox( bb.offset( x, y, z ), 255, 0, 0, 255 );

		GlStateManager.shadeModel( Minecraft.isAmbientOcclusionEnabled() ? GL11.GL_SMOOTH : GL11.GL_FLAT );
		GlStateManager.enableDepth();
		GlStateManager.depthMask( true );
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		if ( entity.renderData == null )
		{
			final BlueprintData data = ChiselsAndBits.getItems().itemBlueprint.getStackData( entity.getItemStack() );
			if ( data != null && data.getState() == EnumLoadState.LOADED )
			{
				entity.renderData = new BlueprintRenderData( data );
			}
		}
	}

	private void adjustAxis(
			final EnumFacing axis,
			final int x,
			final int y,
			final int z )
	{
		final double coef = 1.0 / 16.0;

		switch ( axis )
		{
			case WEST:
				GlStateManager.translate( ( x + 16 ) * coef, 0, 0 );
				break;
			case DOWN:
				GlStateManager.translate( 0, ( y + 16 ) * coef, 0 );
				break;
			case NORTH:
				GlStateManager.translate( 0, 0, ( z + 16 ) * coef );
				break;
			default:
				break;
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(
			final EntityBlueprint entity )
	{
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

}
