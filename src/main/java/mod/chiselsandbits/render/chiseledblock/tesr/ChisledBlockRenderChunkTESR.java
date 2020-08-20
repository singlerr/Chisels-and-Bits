package mod.chiselsandbits.render.chiseledblock.tesr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.google.common.base.Stopwatch;

import mod.chiselsandbits.chiseledblock.EnumTESRRenderState;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.core.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class ChisledBlockRenderChunkTESR extends TileEntityRenderer<TileEntityBlockChiseledTESR>
{
	public final static AtomicInteger pendingTess = new AtomicInteger( 0 );
	public final static AtomicInteger activeTess = new AtomicInteger( 0 );

	private final static ThreadPoolExecutor pool;
	private static ChisledBlockRenderChunkTESR instance;

	static int TESR_Regions_rendered = 0;
	static int TESR_SI_Regions_rendered = 0;

	private void markRendered(
			final boolean singleInstanceMode )
	{
		if ( singleInstanceMode )
		{
			++TESR_SI_Regions_rendered;
		}
		else
		{
			++TESR_Regions_rendered;
		}
	}

	public static ChisledBlockRenderChunkTESR getInstance()
	{
		return instance;
	}

	private static class WorldTracker
	{
		private final LinkedList<FutureTracker> futureTrackers = new LinkedList<FutureTracker>();
		private final Queue<UploadTracker> uploaders = new ConcurrentLinkedQueue<UploadTracker>();
		private final Queue<Runnable> nextFrameTasks = new ConcurrentLinkedQueue<Runnable>();
	};

	private static final WeakHashMap<World, WorldTracker> worldTrackers = new WeakHashMap<World, WorldTracker>();

	private static WorldTracker getTracker()
	{
		final World w = ClientSide.instance.getPlayer().getEntityWorld();
		WorldTracker t = worldTrackers.get( w );

		if ( t == null )
		{
			worldTrackers.put( w, t = new WorldTracker() );
		}

		return t;
	}

	public static void addNextFrameTask(
			final Runnable r )
	{
		getTracker().nextFrameTasks.offer( r );
	}

	private static class FutureTracker
	{
		final TileLayerRenderCache    tlrc;
		final TileRenderCache         renderCache;
		final RenderType              layer;
		final FutureTask<Tessellator> future;

		public FutureTracker(
				final TileLayerRenderCache tlrc,
				final TileRenderCache renderCache,
				final RenderType layer )
		{
			this.tlrc = tlrc;
			this.renderCache = renderCache;
			this.layer = layer;
			future = tlrc.future;
		}

		public void done()
		{
			pendingTess.decrementAndGet();
		}
	};

	private void addFutureTracker(
			final TileLayerRenderCache tlrc,
			final TileRenderCache renderCache,
			final RenderType layer )
	{
		getTracker().futureTrackers.add( new FutureTracker( tlrc, renderCache, layer ) );
	}

	private boolean handleFutureTracker(
			final FutureTracker ft )
	{
		// next frame..?
		if ( ft.future != null && ft.future.isDone() )
		{
			try
			{
				final Tessellator t = ft.future.get();

				if ( ft.future == ft.tlrc.future )
				{
					ft.tlrc.waiting = true;
					getTracker().uploaders.offer( new UploadTracker( ft.renderCache, ft.layer, t ) );
				}
				else
				{
					try
					{
						t.getBuffer().finishDrawing();
					}
					catch ( final IllegalStateException e )
					{
						Log.logError( "Bad Tessellator Behavior.", e );
					}

					ChisledBlockBackgroundRender.submitTessellator( t );
				}
			}
			catch ( final InterruptedException e )
			{
				Log.logError( "Failed to get TESR Future - C", e );
			}
			catch ( final ExecutionException e )
			{
				Log.logError( "Failed to get TESR Future - D", e );
			}
			catch ( final CancellationException e )
			{
				// no issues here.
			}
			finally
			{
				if ( ft.future == ft.tlrc.future )
				{
					ft.tlrc.future = null;
				}
			}

			ft.done();
			return true;
		}

		return false;
	}

	boolean runUpload = false;

	@SubscribeEvent
	public void debugScreen(
			final RenderGameOverlayEvent.Text t )
	{
		if ( Minecraft.getInstance().gameSettings.showDebugInfo )
		{
			if ( TESR_Regions_rendered > 0 || TESR_SI_Regions_rendered > 0 )
			{
				t.getRight().add( "C&B DynRender: " + TESR_Regions_rendered + ":" + TESR_SI_Regions_rendered + " - " + ( GfxRenderState.useVBO() ? "VBO" : "DspList" ) );
				TESR_Regions_rendered = 0;
				TESR_SI_Regions_rendered = 0;
			}
		}
		else
		{
			TESR_Regions_rendered = 0;
			TESR_SI_Regions_rendered = 0;
		}
	}

	int lastFancy = -1;

	@SubscribeEvent
	public void nextFrame(
			final RenderWorldLastEvent e )
	{
		runJobs( getTracker().nextFrameTasks );

		uploadDisplaylists();

		// this seemingly stupid check fixes leaves, other wise we use fast
		// until the atlas refreshes.
		final int currentFancy = Minecraft.getInstance().gameSettings.graphicFanciness.func_238162_a_() > 0 ? 1 : 0;
		if ( currentFancy != lastFancy )
		{
			lastFancy = currentFancy;

			// destroy the cache, and start over.
			ChiselsAndBits.getInstance().clearCache();

			// another dumb thing, MC has probobly already tried reloading
			// things, so we need to tell it to start that over again.
			Minecraft mc = Minecraft.getInstance();
			mc.worldRenderer.loadRenderers();
		}
	}

	private void uploadDisplaylists()
	{
		final WorldTracker trackers = getTracker();

		final Iterator<FutureTracker> i = trackers.futureTrackers.iterator();
		while ( i.hasNext() )
		{
			if ( handleFutureTracker( i.next() ) )
			{
				i.remove();
			}
		}

		final Stopwatch w = Stopwatch.createStarted();
		final boolean dynamicRenderFullChunksOnly = ChiselsAndBits.getConfig().dynamicRenderFullChunksOnly;
		final int maxMillisecondsPerBlock = ChiselsAndBits.getConfig().maxMillisecondsPerBlock;
		final int maxMillisecondsUploadingPerFrame = ChiselsAndBits.getConfig().maxMillisecondsUploadingPerFrame;

		do
		{
			final UploadTracker t = trackers.uploaders.poll();

			if ( t == null )
			{
				return;
			}

			if ( t.trc instanceof TileRenderChunk )
			{
				final Stopwatch sw = Stopwatch.createStarted();
				uploadDisplayList( t );

				if ( !dynamicRenderFullChunksOnly && sw.elapsed( TimeUnit.MILLISECONDS ) > maxMillisecondsPerBlock )
				{
					( (TileRenderChunk) t.trc ).singleInstanceMode = true;
				}
			}
			else
			{
				uploadDisplayList( t );
			}

			t.trc.getLayer( t.layer ).waiting = false;
		}
		while ( w.elapsed( TimeUnit.MILLISECONDS ) < maxMillisecondsUploadingPerFrame );

	}

	private void runJobs(
			final Queue<Runnable> tasks )
	{
		do
		{
			final Runnable x = tasks.poll();

			if ( x == null )
			{
				break;
			}

			x.run();
		}
		while ( true );
	}

	private void uploadDisplayList(
			final UploadTracker t )
	{
		final RenderType layer = t.layer;
		final TileLayerRenderCache tlrc = t.trc.getLayer( layer );

		final Tessellator tx = t.getTessellator();

		if ( tlrc.displayList == null )
		{
			tlrc.displayList = GfxRenderState.getNewState( tx.getBuffer().vertexCount );
		}

		tlrc.displayList = tlrc.displayList.prepare( tx );

		t.submitForReuse();
	}

	public ChisledBlockRenderChunkTESR(final TileEntityRendererDispatcher dispatcher)
	{
        super(dispatcher);
        instance = this;
		ChiselsAndBits.registerWithBus( this );
	}

	static
	{
		final ThreadFactory threadFactory = new ThreadFactory() {

			@Override
			public Thread newThread(
					final Runnable r )
			{
				final Thread t = new Thread( r );
				t.setPriority( Thread.NORM_PRIORITY - 1 );
				t.setName( "C&B Dynamic Render Thread" );
				return t;
			}
		};

		int processors = Runtime.getRuntime().availableProcessors();
		if ( ChiselsAndBits.getConfig().lowMemoryMode )
		{
			processors = 1;
		}

		pool = new ThreadPoolExecutor( 1, processors, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>( 64 ), threadFactory );
		pool.allowCoreThreadTimeOut( false );
	}

	public void bindTexture(
	  ResourceLocation location
    ) {
	    Minecraft.getInstance().getTextureManager().bindTexture(location);
    }

	private void renderTileEntityInner(
      final TileEntityBlockChiseledTESR tileEntityIn,
      final float partialTicks,
      final MatrixStack matrixStackIn,
      final IRenderTypeBuffer bufferIn,
      final int combinedLightIn,
      final int combinedOverlayIn)
	{
		renderLogic( tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, true );
	}

	private void renderLogic(
      final TileEntityBlockChiseledTESR tileEntityIn,
      final float partialTicks,
      final MatrixStack matrixStackIn,
      final IRenderTypeBuffer bufferIn,
      final int combinedLightIn,
      final int combinedOverlayIn,
      final boolean groupLogic)
	{
		final RenderType layer = MinecraftForgeClient.getRenderLayer();
		final TileRenderChunk renderChunk = tileEntityIn.getRenderChunk();
		TileRenderCache renderCache = renderChunk;

		/// how????
		if ( renderChunk == null )
		{
			return;
		}

		// cache at the tile level rather than the chunk level.
		if ( renderChunk.singleInstanceMode )
		{
			if ( groupLogic )
			{
				final EnumTESRRenderState state = renderCache.update( layer, 0 );
				if ( renderCache == null || state == EnumTESRRenderState.SKIP )
				{
					return;
				}

				final TileList tiles = renderChunk.getTiles();
				tiles.getReadLock().lock();

				try
				{
					for ( final TileEntityBlockChiseledTESR e : tiles )
					{
						configureGLState( layer );
						renderLogic(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false);
						unconfigureGLState(layer);
					}
				}
				finally
				{
					tiles.getReadLock().unlock();
				}

				return;
			}

			renderCache = tileEntityIn.getCache();
		}

		final EnumTESRRenderState state = renderCache.update( layer, 0 );
		if ( renderCache == null || state == EnumTESRRenderState.SKIP )
		{
			return;
		}

		final BlockPos chunkOffset = renderChunk.chunkOffset();

		final TileLayerRenderCache tlrc = renderCache.getLayer( layer );
		final boolean isNew = tlrc.isNew();
		boolean hasSubmitted = false;

		if ( tlrc.displayList == null || tlrc.rebuild )
		{
			final int dynamicTess = getMaxTessalators();

			if ( pendingTess.get() < dynamicTess && tlrc.future == null && !tlrc.waiting || isNew )
			{
				// copy the tiles for the thread..
				final ChunkRenderCache cache = ChunkRenderCache.generateCache( tileEntityIn.getWorld(), chunkOffset, chunkOffset.add( 16, 16, 16 ), 1 );
				final FutureTask<Tessellator> newFuture = new FutureTask<Tessellator>( new ChisledBlockBackgroundRender( cache, chunkOffset, renderCache.getTileList(), layer ) );

				try
				{
					pool.submit( newFuture );
					hasSubmitted = true;

					if ( tlrc.future != null )
					{
						tlrc.future.cancel( true );
					}

					tlrc.rebuild = false;
					tlrc.future = newFuture;
					pendingTess.incrementAndGet();
				}
				catch ( final RejectedExecutionException err )
				{
					// Yar...
				}
			}
		}

		// now..
		if ( tlrc.future != null && isNew && hasSubmitted )
		{
			try
			{
				final Tessellator tess = tlrc.future.get( ChiselsAndBits.getConfig().minimizeLatancyMaxTime, TimeUnit.MILLISECONDS );
				tlrc.future = null;
				pendingTess.decrementAndGet();

				uploadDisplayList( new UploadTracker( renderCache, layer, tess ) );

				tlrc.waiting = false;
			}
			catch ( final InterruptedException e )
			{
				Log.logError( "Failed to get TESR Future - A", e );
				tlrc.future = null;
			}
			catch ( final ExecutionException e )
			{
				Log.logError( "Failed to get TESR Future - B", e );
				tlrc.future = null;
			}
			catch ( final TimeoutException e )
			{
				addFutureTracker( tlrc, renderCache, layer );
			}
		}
		else if ( tlrc.future != null && hasSubmitted )
		{
			addFutureTracker( tlrc, renderCache, layer );
		}

		final GfxRenderState dl = tlrc.displayList;
		if ( dl != null && dl.shouldRender() )
		{
			if ( !dl.validForUse() )
			{
				tlrc.displayList = null;
				return;
			}

			matrixStackIn.push();

			final Entity entity = Minecraft.getInstance().getRenderViewEntity();
            double x = entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * (double)partialTicks;
            double y = entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * (double)partialTicks;
            double z = entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * (double)partialTicks;

            matrixStackIn.translate( -x + chunkOffset.getX(),
					-y + chunkOffset.getY(),
					-z + chunkOffset.getZ() );

			configureGLState( layer );

			if ( dl.render(matrixStackIn.getLast().getMatrix()) )
			{
				markRendered( renderChunk.singleInstanceMode );
			}

			unconfigureGLState( layer );

			matrixStackIn.pop();
		}
	}

	public static int getMaxTessalators()
	{
		int dynamicTess = ChiselsAndBits.getConfig().dynamicMaxConcurrentTessalators;

		if ( ChiselsAndBits.getConfig().lowMemoryMode )
		{
			dynamicTess = Math.min( 2, dynamicTess );
		}

		return dynamicTess;
	}

	int isConfigured = 0;

	private void configureGLState(
			final RenderType layer )
	{
		isConfigured++;

		if ( isConfigured == 1 )
		{
		    layer.setupRenderState();
		}
	}

	private void unconfigureGLState(
	  final RenderType layer
    )
	{
		isConfigured--;

		if ( isConfigured > 0 )
		{
			return;
		}

        layer.clearRenderState();
	}

    @Override
    public void render(
      final TileEntityBlockChiseledTESR tileEntityIn,
      final float partialTicks,
      final MatrixStack matrixStackIn,
      final IRenderTypeBuffer bufferIn,
      final int combinedLightIn,
      final int combinedOverlayIn)
    {

    }


}
