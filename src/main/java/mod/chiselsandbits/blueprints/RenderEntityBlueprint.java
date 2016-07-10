package mod.chiselsandbits.blueprints;

import org.lwjgl.opengl.GL11;

import mod.chiselsandbits.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

public class RenderEntityBlueprint extends Render<EntityBlueprint>
{

	EntityItem ei;
	RenderEntityItem rei;

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
	}

	@Override
	protected ResourceLocation getEntityTexture(
			final EntityBlueprint entity )
	{
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

}
