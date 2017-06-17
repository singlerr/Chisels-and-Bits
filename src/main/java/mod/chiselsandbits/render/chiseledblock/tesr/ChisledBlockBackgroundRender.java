package mod.chiselsandbits.render.chiseledblock.tesr;

import java.lang.ref.SoftReference;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import org.lwjgl.opengl.GL11;

import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import mod.chiselsandbits.chiseledblock.data.VoxelNeighborRenderTracker;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.render.chiseledblock.ChiselLayer;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBaked;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ChisledBlockBackgroundRender implements Callable<Tessellator>
{

	private final List<TileEntityBlockChiseledTESR> myPrivateList;
	private final BlockRenderLayer layer;
	private final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
	private final static Queue<CBTessellatorRefHold> previousTessellators = new LinkedBlockingQueue<CBTessellatorRefHold>();

	private final ChunkCache cache;
	private final BlockPos chunkOffset;

	static class CBTessellatorRefNode
	{

		boolean done = false;

		public CBTessellatorRefNode()
		{
			ChisledBlockRenderChunkTESR.activeTess.incrementAndGet();
		}

		public void dispose()
		{
			if ( !done )
			{
				ChisledBlockRenderChunkTESR.activeTess.decrementAndGet();
				done = true;
			}
		}

		@Override
		protected void finalize() throws Throwable
		{
			dispose();
		}

	};

	static class CBTessellatorRefHold
	{
		SoftReference<Tessellator> myTess;
		CBTessellatorRefNode node;

		public CBTessellatorRefHold(
				final CBTessellator cbTessellator )
		{
			myTess = new SoftReference<Tessellator>( cbTessellator );
			node = cbTessellator.node;
		}

		public Tessellator get()
		{
			if ( myTess != null )
			{
				return myTess.get();
			}

			return null;
		}

		public void dispose()
		{
			if ( myTess != null )
			{
				node.dispose();
				myTess = null;
			}
		}

		@Override
		protected void finalize() throws Throwable
		{
			dispose();
		}

	};

	static class CBTessellator extends Tessellator
	{

		CBTessellatorRefNode node = new CBTessellatorRefNode();

		public CBTessellator(
				final int bufferSize )
		{
			super( bufferSize );
		}

	};

	public ChisledBlockBackgroundRender(
			final ChunkCache cache,
			final BlockPos chunkOffset,
			final List<TileEntityBlockChiseledTESR> myList,
			final BlockRenderLayer layer )
	{
		myPrivateList = myList;
		this.layer = layer;
		this.cache = cache;
		this.chunkOffset = chunkOffset;
	}

	public static void submitTessellator(
			final Tessellator t )
	{
		if ( t instanceof CBTessellator )
		{
			previousTessellators.add( new CBTessellatorRefHold( (CBTessellator) t ) );
		}
		else
		{
			throw new RuntimeException( "Invalid TESS submtied for re-use." );
		}
	}

	@Override
	public Tessellator call() throws Exception
	{
		Tessellator tessellator = null;

		do
		{
			do
			{
				final CBTessellatorRefHold holder = previousTessellators.poll();

				if ( holder != null )
				{
					tessellator = holder.get();

					if ( tessellator == null )
					{
						holder.dispose();
					}
				}
			}
			while ( tessellator == null && !previousTessellators.isEmpty() );

			// no previous queues?
			if ( tessellator == null )
			{
				synchronized ( CBTessellator.class )
				{
					if ( ChisledBlockRenderChunkTESR.activeTess.get() < ChisledBlockRenderChunkTESR.getMaxTessalators() )
					{
						tessellator = new CBTessellator( 2109952 );
					}
					else
					{
						Thread.sleep( 10 );
					}
				}
			}
		}
		while ( tessellator == null );

		final BufferBuilder buffer = tessellator.getBuffer();

		try
		{
			buffer.begin( GL11.GL_QUADS, DefaultVertexFormats.BLOCK );
			buffer.setTranslation( -chunkOffset.getX(), -chunkOffset.getY(), -chunkOffset.getZ() );
		}
		catch ( final IllegalStateException e )
		{
			Log.logError( "Invalid Tessellator Behavior", e );
		}

		final int[] faceCount = new int[BlockRenderLayer.values().length];

		final EnumSet<BlockRenderLayer> mcLayers = EnumSet.noneOf( BlockRenderLayer.class );
		final EnumSet<ChiselLayer> layers = layer == BlockRenderLayer.TRANSLUCENT ? EnumSet.of( ChiselLayer.TRANSLUCENT ) : EnumSet.complementOf( EnumSet.of( ChiselLayer.TRANSLUCENT ) );
		for ( final TileEntityBlockChiseled tx : myPrivateList )
		{
			if ( tx instanceof TileEntityBlockChiseledTESR && !tx.isInvalid() )
			{
				final IExtendedBlockState estate = ( (TileEntityBlockChiseledTESR) tx ).getTileRenderState( cache );

				mcLayers.clear();
				for ( final ChiselLayer lx : layers )
				{
					mcLayers.add( lx.layer );
					final ChiseledBlockBaked model = ChiseledBlockSmartModel.getCachedModel( tx, lx );
					faceCount[lx.layer.ordinal()] += model.faceCount();

					if ( !model.isEmpty() )
					{
						blockRenderer.getBlockModelRenderer().renderModel( cache, model, estate, tx.getPos(), buffer, true );

						if ( Thread.interrupted() )
						{
							buffer.finishDrawing();
							submitTessellator( tessellator );
							return null;
						}
					}
				}

				final VoxelNeighborRenderTracker rTracker = estate.getValue( BlockChiseled.UProperty_VoxelNeighborState );
				if ( rTracker != null )
				{
					for ( final BlockRenderLayer brl : mcLayers )
					{
						rTracker.setAbovelimit( brl, faceCount[brl.ordinal()] );
						faceCount[brl.ordinal()] = 0;
					}
				}
			}
		}

		if ( Thread.interrupted() )
		{
			buffer.finishDrawing();
			submitTessellator( tessellator );
			return null;
		}

		return tessellator;
	}

}
