package mod.chiselsandbits.render.chiseledblock.tesr;

import org.lwjgl.opengl.GL11;

import mod.chiselsandbits.config.ModConfig;
import mod.chiselsandbits.core.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public abstract class GfxRenderState
{

	public static enum UseVBO
	{
		AUTOMATIC,
		YES,
		NO
	};

	public static int gfxRefresh = 0;

	public abstract boolean validForUse();

	public abstract boolean render();

	public abstract GfxRenderState prepare(
			final Tessellator t );

	public abstract void destroy();

	public boolean shouldRender()
	{
		return true;
	}

	static public boolean useVBO()
	{
		if ( ModConfig.useVBO == UseVBO.AUTOMATIC )
		{
			return Minecraft.getMinecraft().gameSettings.useVbo;
		}

		return ModConfig.useVBO == UseVBO.YES;
	}

	private static class displayListCleanup implements Runnable
	{

		final int dspList;

		public displayListCleanup(
				final int x )
		{
			dspList = x;
		}

		@Override
		public void run()
		{
			GLAllocation.deleteDisplayLists( dspList );
		}

	};

	private static class vertexBufferCleanup implements Runnable
	{

		final VertexBuffer vertBuffer;

		public vertexBufferCleanup(
				final VertexBuffer buffer )
		{
			vertBuffer = buffer;
		}

		@Override
		public void run()
		{
			vertBuffer.deleteGlBuffers();
		}

	};

	public static GfxRenderState getNewState(
			final int vertexCount )
	{
		if ( vertexCount == 0 )
		{
			return new VoidRenderState();
		}
		else
		{
			if ( useVBO() )
			{
				return new VBORenderState();
			}

			return new DisplayListRenderState();
		}
	}

	public static class VoidRenderState extends GfxRenderState
	{

		@Override
		public boolean validForUse()
		{
			return true;
		}

		@Override
		public boolean render()
		{
			return false;
		}

		@Override
		public boolean shouldRender()
		{
			return false;
		}

		@Override
		public GfxRenderState prepare(
				final Tessellator t )
		{
			final int vc = t.getBuffer().getVertexCount();

			if ( vc > 0 )
			{
				return GfxRenderState.getNewState( vc ).prepare( t );
			}

			t.getBuffer().finishDrawing();
			return this;
		}

		@Override
		public void destroy()
		{
		}

	};

	public static class DisplayListRenderState extends GfxRenderState
	{

		int refreshNum = 0;
		int displayList = 0;

		@Override
		public boolean validForUse()
		{
			return useVBO() == false && refreshNum == gfxRefresh;
		}

		@Override
		public boolean render()
		{
			if ( displayList != 0 )
			{
				GlStateManager.callList( displayList );
				return true;
			}

			return false;
		}

		@Override
		public GfxRenderState prepare(
				final Tessellator t )
		{
			if ( t.getBuffer().getVertexCount() == 0 )
			{
				destroy();
				return new VoidRenderState().prepare( t );
			}

			if ( displayList == 0 )
			{
				displayList = GLAllocation.generateDisplayLists( 1 );
			}

			try
			{
				GlStateManager.glNewList( displayList, GL11.GL_COMPILE );
				t.draw();
				refreshNum = gfxRefresh;
			}
			catch ( final IllegalStateException e )
			{
				Log.logError( "Erratic Tessellator Behavior", e );
				destroy();
				return null;
			}
			finally
			{
				GlStateManager.glEndList();
			}

			return this;
		}

		@Override
		protected void finalize() throws Throwable
		{
			if ( displayList != 0 )
			{
				ChisledBlockRenderChunkTESR.addNextFrameTask( new displayListCleanup( displayList ) );
			}
		}

		@Override
		public void destroy()
		{
			if ( displayList != 0 )
			{
				GLAllocation.deleteDisplayLists( displayList );
				displayList = 0;
			}
		}

	};

	public static class VBORenderState extends GfxRenderState
	{

		int refreshNum;
		VertexBuffer vertexbuffer;

		@Override
		public boolean validForUse()
		{
			return useVBO() && refreshNum == gfxRefresh;
		}

		@Override
		public GfxRenderState prepare(
				final Tessellator t )
		{
			if ( t.getBuffer().getVertexCount() == 0 )
			{
				destroy();
				return new VoidRenderState().prepare( t );
			}

			destroy();

			if ( vertexbuffer == null )
			{
				vertexbuffer = new VertexBuffer( t.getBuffer().getVertexFormat() );
			}

			t.getBuffer().finishDrawing();
			vertexbuffer.bufferData( t.getBuffer().getByteBuffer() );
			refreshNum = gfxRefresh;

			return this;
		}

		@Override
		public boolean render()
		{
			if ( vertexbuffer != null )
			{
				GlStateManager.glEnableClientState( 32884 );
				OpenGlHelper.setClientActiveTexture( OpenGlHelper.defaultTexUnit );
				GlStateManager.glEnableClientState( 32888 );
				OpenGlHelper.setClientActiveTexture( OpenGlHelper.lightmapTexUnit );
				GlStateManager.glEnableClientState( 32888 );
				OpenGlHelper.setClientActiveTexture( OpenGlHelper.defaultTexUnit );
				GlStateManager.glEnableClientState( 32886 );

				vertexbuffer.bindBuffer();
				setupArrayPointers();
				vertexbuffer.drawArrays( GL11.GL_QUADS );
				OpenGlHelper.glBindBuffer( OpenGlHelper.GL_ARRAY_BUFFER, 0 );
				GlStateManager.resetColor();

				for ( final VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements() )
				{
					final VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
					final int i = vertexformatelement.getIndex();

					switch ( vertexformatelement$enumusage )
					{
						case POSITION:
							GlStateManager.glDisableClientState( 32884 );
							break;
						case UV:
							OpenGlHelper.setClientActiveTexture( OpenGlHelper.defaultTexUnit + i );
							GlStateManager.glDisableClientState( 32888 );
							OpenGlHelper.setClientActiveTexture( OpenGlHelper.defaultTexUnit );
							break;
						case COLOR:
							GlStateManager.glDisableClientState( 32886 );
							GlStateManager.resetColor();
						default:
							break;
					}
				}

				return true;
			}

			return false;
		}

		private void setupArrayPointers()
		{
			GlStateManager.glVertexPointer( 3, GL11.GL_FLOAT, 28, 0 );
			GlStateManager.glColorPointer( 4, GL11.GL_UNSIGNED_BYTE, 28, 12 );
			GlStateManager.glTexCoordPointer( 2, GL11.GL_FLOAT, 28, 16 );
			OpenGlHelper.setClientActiveTexture( OpenGlHelper.lightmapTexUnit );
			GlStateManager.glTexCoordPointer( 2, GL11.GL_SHORT, 28, 24 );
			OpenGlHelper.setClientActiveTexture( OpenGlHelper.defaultTexUnit );
		}

		@Override
		protected void finalize() throws Throwable
		{
			if ( vertexbuffer != null )
			{
				ChisledBlockRenderChunkTESR.addNextFrameTask( new vertexBufferCleanup( vertexbuffer ) );
			}
		}

		@Override
		public void destroy()
		{
			if ( vertexbuffer != null )
			{
				vertexbuffer.deleteGlBuffers();
				vertexbuffer = null;
			}
		}

	}

}
