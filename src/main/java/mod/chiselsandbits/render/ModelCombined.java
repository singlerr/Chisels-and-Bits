package mod.chiselsandbits.render;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.core.ClientSide;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;

public class ModelCombined extends BaseBakedBlockModel
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
	public TextureAtlasSprite getParticleTexture()
	{
		for ( final IFlexibleBakedModel a : merged )
		{
			return a.getParticleTexture();
		}

		return ClientSide.instance.getMissingIcon();
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
		for ( final IFlexibleBakedModel a : merged )
		{
			return a.getFormat();
		}

		return DefaultVertexFormats.ITEM;
	}

}
