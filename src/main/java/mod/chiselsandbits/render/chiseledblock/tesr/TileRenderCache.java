package mod.chiselsandbits.render.chiseledblock.tesr;

import java.util.List;

import mod.chiselsandbits.chiseledblock.EnumTESRRenderState;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import mod.chiselsandbits.core.ClientSide;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.MinecraftForgeClient;

public abstract class TileRenderCache
{

	private final TileLayerRenderCache solid = new TileLayerRenderCache();
	private final TileLayerRenderCache translucent = new TileLayerRenderCache();

	public TileLayerRenderCache getLayer(
			final EnumWorldBlockLayer layer )
	{
		if ( layer == EnumWorldBlockLayer.TRANSLUCENT )
		{
			return translucent;
		}

		return solid;
	}

	public abstract List<TileEntityBlockChiseledTESR> getTiles();

	public EnumTESRRenderState update(
			final EnumWorldBlockLayer layer,
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
		final EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderPass() == 0 ? EnumWorldBlockLayer.SOLID : EnumWorldBlockLayer.TRANSLUCENT;
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
