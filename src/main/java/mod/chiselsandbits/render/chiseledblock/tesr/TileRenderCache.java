package mod.chiselsandbits.render.chiseledblock.tesr;

import mod.chiselsandbits.chiseledblock.EnumTESRRenderState;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import mod.chiselsandbits.core.ClientSide;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.MinecraftForgeClient;

import java.util.List;

public abstract class TileRenderCache
{

	private final TileLayerRenderCache solid = new TileLayerRenderCache();
	private final TileLayerRenderCache translucent = new TileLayerRenderCache();

	public TileLayerRenderCache getLayer(
			final RenderType layer )
	{
		if ( layer == RenderType.getTranslucent() )
		{
			return translucent;
		}

		return solid;
	}

	abstract public List<TileEntityBlockChiseledTESR> getTileList();

	public EnumTESRRenderState update(
			final RenderType layer,
			final int updateCost )
	{
		final int lastRF = ClientSide.instance.getLastRenderedFrame();
		final TileLayerRenderCache tlrc = getLayer( layer );

		// render?
		if ( layer != null && tlrc.lastRenderedFrame != lastRF )
		{
			tlrc.lastRenderedFrame = lastRF;
			return EnumTESRRenderState.RENDER;
		}

		return EnumTESRRenderState.SKIP;
	}

	public boolean hasRenderedThisFrame()
	{
		final RenderType layer = MinecraftForgeClient.getRenderLayer();
		final TileLayerRenderCache tlrc = getLayer( layer );

		final int lastRF = ClientSide.instance.getLastRenderedFrame();
		return !( layer != null && tlrc.lastRenderedFrame != lastRF );
	}

	public void rebuild(
			final boolean convert )
	{
		solid.rebuild = true;
		translucent.rebuild = true;

		if ( convert )
		{
			solid.conversion = true;
			translucent.conversion = true;
		}
	}

}
