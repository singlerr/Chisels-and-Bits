package mod.chiselsandbits.blueprints;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;

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
		ei.setEntityItemStack( entity.item );
		rei.doRender( ei, x, y, z, entityYaw, partialTicks );
	}

	@Override
	protected ResourceLocation getEntityTexture(
			final EntityBlueprint entity )
	{
		// TODO Auto-generated method stub
		return null;
	}

}
