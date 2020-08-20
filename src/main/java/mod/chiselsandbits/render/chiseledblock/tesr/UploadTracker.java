package mod.chiselsandbits.render.chiseledblock.tesr;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;

class UploadTracker
{
	final   TileRenderCache trc;
	final   RenderType      layer;
	private Tessellator     src;

	public Tessellator getTessellator()
	{
		if ( src == null )
		{
			throw new NullPointerException();
		}
		return src;
	}

	public UploadTracker(
			final TileRenderCache t,
			final RenderType l,
			final Tessellator tess )
	{
		trc = t;
		layer = l;
		src = tess;
	}

	public void submitForReuse()
	{
		ChisledBlockBackgroundRender.submitTessellator( src );
		src = null;
	}
}