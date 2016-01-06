package mod.chiselsandbits.integration.mcmultipart;

import mcmultipart.client.multipart.MultipartSpecialRenderer;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import mod.chiselsandbits.render.chiseledblock.tesr.ChisledBlockRenderChunkTESR;

class ChisledBlockRenderChunkMPSR extends MultipartSpecialRenderer<ChisledBlockPart>
{
	@Override
	public boolean canRenderBreaking(
			final ChisledBlockPart part )
	{
		return part.getTile() instanceof TileEntityBlockChiseledTESR;
	}

	@Override
	public void renderMultipartAt(
			final ChisledBlockPart part,
			final double x,
			final double y,
			final double z,
			final float partialTicks,
			final int destroyStage )
	{
		if ( part.getTile() instanceof TileEntityBlockChiseledTESR )
		{
			ChisledBlockRenderChunkTESR.getInstance().renderTileEntityAt( (TileEntityBlockChiseledTESR) part.getTile(), x, y, z, partialTicks, destroyStage );
		}
	}

}
