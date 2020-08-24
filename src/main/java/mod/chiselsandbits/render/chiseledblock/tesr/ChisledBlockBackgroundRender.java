package mod.chiselsandbits.render.chiseledblock.tesr;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import mod.chiselsandbits.utils.ChuckRenderCacheWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
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
import net.minecraft.util.math.BlockPos;

public class ChisledBlockBackgroundRender implements Callable<Tessellator>
{

	private final List<TileEntityBlockChiseledTESR> myPrivateList;
	private final RenderType layer;
	private final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
	private final static Queue<CBTessellatorRefHold> previousTessellators = new LinkedBlockingQueue<CBTessellatorRefHold>();

	private final ChunkRenderCache cache;
	private final BlockPos chunkOffset;

	private final static ThreadLocal<Random> BACKGROUND_RANDOM = ThreadLocal.withInitial(Random::new);

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
			final ChunkRenderCache cache,
			final BlockPos chunkOffset,
			final List<TileEntityBlockChiseledTESR> myList,
			final RenderType layer )
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
        final IVertexBuilder transformBuilder = new MatrixApplyingVertexBuilder(buffer, Matrix4f.makeTranslate(-chunkOffset.getX(), -chunkOffset.getY(), -chunkOffset.getZ()), new Matrix3f());
		try
		{
			buffer.begin( GL11.GL_QUADS, DefaultVertexFormats.BLOCK );
		}
		catch ( final IllegalStateException e )
		{
			Log.logError( "Invalid Tessellator Behavior", e );
		}

		final Map<RenderType, Integer> faceCount = Maps.newHashMap();

		final Set<RenderType> mcLayers = Sets.newHashSet();
		final EnumSet<ChiselLayer> layers = layer == RenderType.getTranslucent() ? EnumSet.of( ChiselLayer.TRANSLUCENT ) : EnumSet.complementOf( EnumSet.of( ChiselLayer.TRANSLUCENT ) );
		for ( final TileEntityBlockChiseled tx : myPrivateList )
		{
			if ( tx instanceof TileEntityBlockChiseledTESR && !tx.isRemoved() )
			{
				final BlockState estate = ( (TileEntityBlockChiseledTESR) tx ).getBlockState( cache );

				mcLayers.clear();
				for ( final ChiselLayer lx : layers )
				{
					mcLayers.add( lx.layer );
					final ChiseledBlockBaked model = ChiseledBlockSmartModel.getCachedModel( tx, lx );
					faceCount.put(lx.layer, faceCount.getOrDefault(lx.layer, 0) + model.faceCount());

					if ( !model.isEmpty() )
					{
						blockRenderer.getBlockModelRenderer().renderModel( new ChuckRenderCacheWrapper(cache),
                          model, estate, tx.getPos(), new MatrixStack(), transformBuilder, true, BACKGROUND_RANDOM.get(), BACKGROUND_RANDOM.get().nextLong(),
                          OverlayTexture.NO_OVERLAY);

						if ( Thread.interrupted() )
						{
							buffer.finishDrawing();
							submitTessellator( tessellator );
							return null;
						}
					}
				}

				final VoxelNeighborRenderTracker rTracker = tx.getNeighborRenderTracker();
				if ( rTracker != null )
				{
					for ( final RenderType brl : mcLayers )
					{
						rTracker.setAbovelimit( brl, faceCount.get(brl) );
						faceCount.remove(brl);
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
