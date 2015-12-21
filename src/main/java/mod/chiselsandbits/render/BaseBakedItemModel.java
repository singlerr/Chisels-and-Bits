package mod.chiselsandbits.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;

@SuppressWarnings( "deprecation" )
public abstract class BaseBakedItemModel implements IFlexibleBakedModel
{
	protected ArrayList<BakedQuad> list = new ArrayList<BakedQuad>();

	@Override
	final public boolean isAmbientOcclusion()
	{
		return true;
	}

	@Override
	final public boolean isGui3d()
	{
		return true;
	}

	@Override
	final public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	final public List<BakedQuad> getFaceQuads(
			final EnumFacing side )
			{
		return Collections.emptyList();
			}

	@Override
	final public List<BakedQuad> getGeneralQuads()
	{
		return list;
	}

	@Override
	final public VertexFormat getFormat()
	{
		return DefaultVertexFormats.ITEM;
	}

	@Override
	final public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}
}
