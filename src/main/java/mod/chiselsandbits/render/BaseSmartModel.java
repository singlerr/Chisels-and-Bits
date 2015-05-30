
package mod.chiselsandbits.render;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;


@SuppressWarnings( "deprecation" )
public abstract class BaseSmartModel implements IFlexibleBakedModel
{

	@Override
	public boolean isAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getTexture()
	{
		return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture( Blocks.stone.getDefaultState() );
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public List<BakedQuad> getFaceQuads(
			final EnumFacing side )
	{
		return Collections.emptyList();
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		return Collections.emptyList();
	}

	@Override
	public VertexFormat getFormat()
	{
		return null;
	}

}
