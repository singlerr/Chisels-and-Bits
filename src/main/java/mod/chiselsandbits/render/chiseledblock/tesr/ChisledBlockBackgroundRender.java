package mod.chiselsandbits.render.chiseledblock.tesr;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.opengl.GL11;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBaked;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RegionRenderCache;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ChisledBlockBackgroundRender implements Callable<Tessellator>
{

	private final List<TileEntityBlockChiseledTESR> myPrivateList;
	private final EnumWorldBlockLayer layer;
	private final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
	private final static Queue<SoftReference<Tessellator>> previousTessellators = new ConcurrentLinkedQueue<SoftReference<Tessellator>>();

	private final RegionRenderCache cache;
	private final BlockPos chunkOffset;

	public ChisledBlockBackgroundRender(
			final RegionRenderCache cache,
			final BlockPos chunkOffset,
			final List<TileEntityBlockChiseledTESR> tiles,
			final EnumWorldBlockLayer layer )
	{
		myPrivateList = new ArrayList<TileEntityBlockChiseledTESR>( tiles );
		this.layer = layer;
		this.cache = cache;
		this.chunkOffset = chunkOffset;
	}

	public static void submitTessellator(
			final Tessellator t )
	{
		previousTessellators.add( new SoftReference<Tessellator>( t ) );
	}

	@Override
	public Tessellator call() throws Exception
	{
		Tessellator tessellator = null;

		do
		{
			final SoftReference<Tessellator> softTessellator = previousTessellators.poll();
			if ( softTessellator != null )
			{
				tessellator = softTessellator.get();
			}
		}
		while ( tessellator == null && !previousTessellators.isEmpty() );

		// no previous queues?
		if ( tessellator == null )
		{
			tessellator = new Tessellator( 2109952 );
		}

		final WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.begin( GL11.GL_QUADS, DefaultVertexFormats.BLOCK );
		worldrenderer.setTranslation( -chunkOffset.getX(), -chunkOffset.getY(), -chunkOffset.getZ() );

		final EnumSet<EnumWorldBlockLayer> layers = layer == EnumWorldBlockLayer.TRANSLUCENT ? EnumSet.of( layer ) : EnumSet.complementOf( EnumSet.of( EnumWorldBlockLayer.TRANSLUCENT ) );
		for ( final TileEntityBlockChiseled tx : myPrivateList )
		{
			if ( tx instanceof TileEntityBlockChiseledTESR && !tx.isInvalid() )
			{
				final IExtendedBlockState estate = ( (TileEntityBlockChiseledTESR) tx ).getTileRenderState();

				for ( final EnumWorldBlockLayer lx : layers )
				{
					final ChiseledBlockBaked model = ChiseledBlockSmartModel.getCachedModel( tx, lx );

					if ( !model.isEmpty() )
					{
						blockRenderer.getBlockModelRenderer().renderModel( cache, model, estate, tx.getPos(), worldrenderer );
					}
				}
			}
		}

		ChisledBlockRenderChunkTESR.activeTess.incrementAndGet();
		return tessellator;
	}

}
