package mod.chiselsandbits.render;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.ClientSide;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;

@SuppressWarnings( "deprecation" )
public class ModelCombined implements IFlexibleBakedModel
{

	IFlexibleBakedModel[] merged;

	List<BakedQuad>[] face;
	List<BakedQuad> generic;

	@SuppressWarnings( "unchecked" )
	public ModelCombined(
			final IFlexibleBakedModel... args )
	{
		face = new ArrayList[EnumFacing.VALUES.length];

		generic = new ArrayList<BakedQuad>();
		for ( final EnumFacing f : EnumFacing.VALUES )
		{
			face[f.ordinal()] = new ArrayList<BakedQuad>();
		}

		merged = args;

		for ( final IFlexibleBakedModel m : merged )
		{
			generic.addAll( m.getGeneralQuads() );
			for ( final EnumFacing f : EnumFacing.VALUES )
			{
				face[f.ordinal()].addAll( m.getFaceQuads( f ) );
			}
		}
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		for ( final IFlexibleBakedModel a : merged )
		{
			if ( a.isAmbientOcclusion() )
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isGui3d()
	{
		for ( final IFlexibleBakedModel a : merged )
		{
			if ( a.isGui3d() )
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		for ( final IFlexibleBakedModel a : merged )
		{
			return a.getParticleTexture();
		}

		return ClientSide.instance.getMissingIcon();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		for ( final IFlexibleBakedModel a : merged )
		{
			return a.getItemCameraTransforms();
		}

		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public List<BakedQuad> getFaceQuads(
			final EnumFacing side )
	{
		return face[side.ordinal()];
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		return generic;
	}

	@Override
	public VertexFormat getFormat()
	{
		return null;
	}

}
