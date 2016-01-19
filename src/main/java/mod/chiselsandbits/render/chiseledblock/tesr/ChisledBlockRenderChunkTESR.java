package mod.chiselsandbits.render.chiseledblock.tesr;

import java.util.EnumSet;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Stopwatch;

import mod.chiselsandbits.chiseledblock.EnumTESRRenderState;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBaked;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChisledBlockRenderChunkTESR extends TileEntitySpecialRenderer<TileEntityBlockChiseledTESR>
{
	public final static AtomicInteger activeTess = new AtomicInteger( 0 );
	private final static ThreadPoolExecutor pool;
	private static ChisledBlockRenderChunkTESR instance;

	public static ChisledBlockRenderChunkTESR getInstance()
	{
		return instance;
	}

	private static class UploadTracker
	{
		final TileRenderCache trc;
		final EnumWorldBlockLayer layer;
		final Tessellator src;

		public UploadTracker(
				final TileRenderCache t,
				final EnumWorldBlockLayer l,
				final Tessellator tess )
		{
			trc = t;
			layer = l;
			src = tess;
		}

	};

	private final Queue<UploadTracker> uploaders = new ConcurrentLinkedQueue<UploadTracker>();
	private final Queue<FutureTask<Tessellator>> canceledUploaders = new ConcurrentLinkedQueue<FutureTask<Tessellator>>();
	private final static Queue<Runnable> nextFrameTasks = new ConcurrentLinkedQueue<Runnable>();

	public static void addTask(
			final Runnable r )
	{
		nextFrameTasks.offer( r );
	}

	@SubscribeEvent
	void uploadDisplaylists(
			final RenderWorldLastEvent e )
	{
		do
		{
			final Runnable x = nextFrameTasks.poll();

			if ( x == null )
			{
				break;
			}

			x.run();
		}
		while ( true );

		final FutureTask<Tessellator> ft = canceledUploaders.poll();
		if ( ft != null && ft.isDone() )
		{
			try
			{
				activeTess.decrementAndGet();

				// mark it as finished, but don't draw it.
				final Tessellator t = ft.get();
				t.getWorldRenderer().finishDrawing();

				ChisledBlockBackgroundRender.submitTessellator( t );
			}
			catch ( final InterruptedException e1 )
			{
				Log.logError( "Failed to get TESR Future - E", e1 );
			}
			catch ( final ExecutionException e1 )
			{
				Log.logError( "Failed to get TESR Future - F", e1 );
			}
		}

		final Stopwatch w = Stopwatch.createStarted();
		do
		{
			final UploadTracker t = uploaders.poll();

			if ( t == null )
			{
				return;
			}

			if ( t.trc instanceof TileRenderChunk )
			{
				final Stopwatch sw = Stopwatch.createStarted();
				uploadDisplayList( t );

				if ( sw.elapsed( TimeUnit.MILLISECONDS ) > 10 )
				{
					( (TileRenderChunk) t.trc ).singleInstanceMode = true;
				}
			}
			else
			{
				uploadDisplayList( t );
			}

			activeTess.decrementAndGet();
			ChisledBlockBackgroundRender.submitTessellator( t.src );

			t.trc.getLayer( t.layer ).waiting = false;
		}
		while ( w.elapsed( TimeUnit.MILLISECONDS ) < 1 );
	}

	private void uploadDisplayList(
			final UploadTracker t )
	{
		final EnumWorldBlockLayer layer = t.layer;
		final TileLayerRenderCache tlrc = t.trc.getLayer( layer );

		if ( tlrc.displayList == 0 )
		{
			tlrc.displayList = GLAllocation.generateDisplayLists( 1 );
		}

		GL11.glNewList( tlrc.displayList, GL11.GL_COMPILE );
		t.src.draw();
		GL11.glEndList();
	}

	public ChisledBlockRenderChunkTESR()
	{
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
		pool = new ThreadPoolExecutor( 1, Runtime.getRuntime().availableProcessors(), 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>( 64 ), threadFactory );
		pool.allowCoreThreadTimeOut( false );
	}

	public void renderBreakingEffects(
			final TileEntityBlockChiseled te,
			final double x,
			final double y,
			final double z,
			final float partialTicks,
			final int destroyStage )
	{
		bindTexture( TextureMap.locationBlocksTexture );
		final String file = DESTROY_STAGES[destroyStage].toString().replace( "textures/", "" ).replace( ".png", "" );
		final TextureAtlasSprite damageTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite( file );

		GlStateManager.pushMatrix();
		GlStateManager.depthFunc( GL11.GL_LEQUAL );
		final BlockPos cp = te.getPos();
		GlStateManager.translate( x - cp.getX(), y - cp.getY(), z - cp.getZ() );

		final Tessellator tessellator = Tessellator.getInstance();
		final WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.begin( GL11.GL_QUADS, DefaultVertexFormats.BLOCK );
		worldrenderer.setTranslation( 0, 0, 0 );

		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		final EnumSet<EnumWorldBlockLayer> layers = EnumSet.allOf( EnumWorldBlockLayer.class );

		final IExtendedBlockState estate = te.getRenderState();

		for ( final EnumWorldBlockLayer lx : layers )
		{
			final ChiseledBlockBaked model = ChiseledBlockSmartModel.getCachedModel( te, lx );

			if ( !model.isEmpty() )
			{
				final IBakedModel damageModel = new SimpleBakedModel.Builder( model, damageTexture ).makeBakedModel();
				blockRenderer.getBlockModelRenderer().renderModel( te.getWorld(), damageModel, estate, te.getPos(), worldrenderer, false );
			}
		}

		tessellator.draw();
		worldrenderer.setTranslation( 0.0D, 0.0D, 0.0D );

		GlStateManager.resetColor();
		GlStateManager.popMatrix();
		return;
	}

	@Override
	public void renderTileEntityAt(
			final TileEntityBlockChiseledTESR te,
			final double x,
			final double y,
			final double z,
			final float partialTicks,
			final int destroyStage )
	{
		final EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderPass() == 0 ? EnumWorldBlockLayer.SOLID : EnumWorldBlockLayer.TRANSLUCENT;
		final TileRenderChunk renderChunk = te.getRenderChunk();
		TileRenderCache renderCache = renderChunk;

		/// how????
		if ( renderChunk == null )
		{
			return;
		}

		if ( destroyStage >= 0 )
		{
			if ( layer == EnumWorldBlockLayer.SOLID )
			{
				return;
			}

			renderBreakingEffects( te, x, y, z, partialTicks, destroyStage );
			return;
		}

		// cache at the tile level rather than the chunk level.
		if ( renderChunk.singleInstanceMode )
		{
			renderCache = te.getCache();
		}

		final EnumTESRRenderState state = renderCache.update( layer, 0 );
		if ( renderCache == null || state == EnumTESRRenderState.SKIP )
		{
			return;
		}

		GL11.glPushMatrix();

		final BlockPos chunkOffset = renderChunk.chunkOffset();
		GL11.glTranslated( -TileEntityRendererDispatcher.staticPlayerX + chunkOffset.getX(),
				-TileEntityRendererDispatcher.staticPlayerY + chunkOffset.getY(),
				-TileEntityRendererDispatcher.staticPlayerZ + chunkOffset.getZ() );

		final TileLayerRenderCache tlrc = renderCache.getLayer( layer );
		final boolean isNew = tlrc.isNew();

		if ( tlrc.displayList == 0 || tlrc.rebuild )
		{
			if ( activeTess.get() < ChiselsAndBits.getConfig().dynamicMaxConcurrentTessalators && tlrc.future == null && !tlrc.waiting || isNew )
			{
				// copy the tiles for the thread..
				final FutureTask<Tessellator> newFuture = new FutureTask<Tessellator>( new ChisledBlockBackgroundRender( chunkOffset, renderCache.getTiles(), layer ) );

				try
				{
					pool.submit( newFuture );

					if ( tlrc.future != null )
					{
						canceledUploaders.offer( tlrc.future );
					}

					tlrc.rebuild = false;
					tlrc.future = newFuture;
				}
				catch ( final RejectedExecutionException err )
				{
					// Yar...
				}
			}
		}

		// now..
		if ( tlrc.future != null && isNew )
		{
			try
			{
				final Tessellator tess = tlrc.future.get();

				uploadDisplayList( new UploadTracker( renderCache, layer, tess ) );
				activeTess.decrementAndGet();
				ChisledBlockBackgroundRender.submitTessellator( tess );

				tlrc.waiting = false;
			}
			catch ( final InterruptedException e )
			{
				Log.logError( "Failed to get TESR Future - A", e );
			}
			catch ( final ExecutionException e )
			{
				Log.logError( "Failed to get TESR Future - B", e );
			}
			finally
			{
				tlrc.future = null;
			}
		}

		// next frame..?
		if ( tlrc.future != null && tlrc.future.isDone() )
		{
			try
			{
				final Tessellator t = tlrc.future.get();

				tlrc.waiting = true;
				uploaders.offer( new UploadTracker( renderCache, layer, t ) );
			}
			catch ( final InterruptedException e )
			{
				Log.logError( "Failed to get TESR Future - C", e );
			}
			catch ( final ExecutionException e )
			{
				Log.logError( "Failed to get TESR Future - D", e );
			}
			finally
			{
				tlrc.future = null;
			}
		}

		final int dl = tlrc.displayList;
		if ( dl != 0 )
		{
			configureGLState( layer );
			GL11.glCallList( dl );
			unconfigureGLState();
		}

		GL11.glPopMatrix();
	}

	private void configureGLState(
			final EnumWorldBlockLayer layer )
	{
		bindTexture( TextureMap.locationBlocksTexture );

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
		GlStateManager.color( 1.0f, 1.0f, 1.0f, 1.0f );

		if ( layer == EnumWorldBlockLayer.TRANSLUCENT )
		{
			GlStateManager.enableBlend();
			GlStateManager.disableAlpha();
		}
		else
		{
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
		}

		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();

		if ( Minecraft.isAmbientOcclusionEnabled() )
		{
			GlStateManager.shadeModel( GL11.GL_SMOOTH );
		}
		else
		{
			GlStateManager.shadeModel( GL11.GL_FLAT );
		}
	}

	private void unconfigureGLState()
	{
		GlStateManager.color( 1.0f, 1.0f, 1.0f, 1.0f );
		GlStateManager.resetColor(); // required to be called after drawing the
										// display list cause the post render
										// method usually calls it.

		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();

		RenderHelper.enableStandardItemLighting();
	}

}
