package mod.chiselsandbits.render.chiseledblock;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

public class UnpackedQuadBuilderWrapper implements IFaceBuilder
{

	UnpackedBakedQuad.Builder builder;

	@Override
	public void begin(
			final VertexFormat format )
	{
		builder = new UnpackedBakedQuad.Builder( format );
	}

	@Override
	public BakedQuad create()
	{
		return builder.build();
	}

	@Override
	public void setFace(
			final EnumFacing myFace )
	{
		builder.setQuadColored();
		builder.setQuadOrientation( myFace );
		builder.setQuadTint( 0 );
	}

	@Override
	public void put(
			final int element,
			final float... args )
	{
		builder.put( element, args );
	}

}
