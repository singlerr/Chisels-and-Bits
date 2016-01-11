package mod.chiselsandbits.render.chiseledblock.tesr;

import java.util.concurrent.FutureTask;

import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;

public class TileLayerRenderCache
{
	public FutureTask<Tessellator> future = null;
	public boolean waiting = false;
	public boolean rebuild = true;
	public int lastRenderedFrame = Integer.MAX_VALUE;
	public int displayList = 0;
	public boolean conversion = true;

	@Override
	protected void finalize() throws Throwable
	{
		if ( displayList != 0 )
		{
			GLAllocation.deleteDisplayLists( displayList );
		}
	}

	public boolean isNew()
	{
		final boolean wasConversion = conversion;
		conversion = false;
		return wasConversion && ChiselsAndBits.getConfig().dynamicModelMinimizeLatancy;
	}

}