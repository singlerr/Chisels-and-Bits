package mod.chiselsandbits.client.model.baked.base;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;

public abstract class BaseBakedBlockModel extends BaseBakedPerspectiveModel implements IBakedModel
{

	@Override
	final public boolean useAmbientOcclusion()
	{
		return true;
	}

	@Override
	final public boolean isGui3d()
	{
		return true;
	}

	@Override
	final public boolean isCustomRenderer()
	{
		return false;
	}

	@SuppressWarnings({"NullableProblems", "deprecation"})
    @Override
	final public ItemCameraTransforms getTransforms()
	{
		return ItemCameraTransforms.NO_TRANSFORMS;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.EMPTY;
	}

}
