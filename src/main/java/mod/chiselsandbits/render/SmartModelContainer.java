package mod.chiselsandbits.render;

import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Function;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

public class SmartModelContainer implements IModel
{
	IBakedModel smartModel;

	public SmartModelContainer(
			final IBakedModel smartModel )
	{
		this.smartModel = smartModel;
	}

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return Collections.emptyList();
	}

	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return Collections.emptyList();
	}

	@Override
	public IBakedModel bake(
			final IModelState state,
			final VertexFormat format,
			final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		return smartModel;
	}

	@Override
	public IModelState getDefaultState()
	{
		return TRSRTransformation.identity();
	}

}
