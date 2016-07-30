package mod.chiselsandbits.render.chiseledblock.tesr;

import java.util.concurrent.FutureTask;

import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.client.renderer.Tessellator;

public class TileLayerRenderCache
{
	public FutureTask<Tessellator> future = null;
	public boolean waiting = false;
	public boolean rebuild = true;
	public int lastRenderedFrame = Integer.MAX_VALUE;
	public GfxRenderState displayList = null;
	public boolean conversion = ChiselsAndBits.getConfig().dynamicModelMinimizeLatancy;

	public boolean isNew()
	{
		final boolean wasConversion = conversion;
		conversion = false;
		return wasConversion && ChiselsAndBits.getConfig().dynamicModelMinimizeLatancy;
	}

}