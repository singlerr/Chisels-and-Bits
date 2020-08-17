package mod.chiselsandbits.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.util.Direction;

public abstract class BaseBakedItemModel extends BaseBakedPerspectiveModel implements IBakedModel
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
	public List<BakedQuad> getQuads(
			final BlockState state,
			final Direction side,
			final long rand )
	{
		if ( side != null )
		{
			return Collections.emptyList();
		}

		return list;
	}

	@Override
	final public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.NONE;
	}
}
