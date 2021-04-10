package mod.chiselsandbits.client.model.baked.base;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;

public abstract class BaseBakedBlockModel extends BaseBakedPerspectiveModel implements IBakedModel
{

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

	@SuppressWarnings({"NullableProblems", "deprecation"})
    @Override
	final public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.EMPTY;
	}

}
