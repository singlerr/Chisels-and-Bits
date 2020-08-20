package mod.chiselsandbits.render.chiseledblock.tesr;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.*;

import mod.chiselsandbits.config.ModConfig;
import mod.chiselsandbits.core.Log;
import net.minecraft.client.renderer.GLAllocation;
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

	public abstract boolean render(Matrix4f matrix4f);

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
			return true;
		}

		return ModConfig.useVBO == UseVBO.YES;
	}

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
			vertBuffer.close();
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
            return new VBORenderState();
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
		public boolean render(final Matrix4f matrix4f)
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
			final int vc = t.getBuffer().vertexCount;

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
			if ( t.getBuffer().vertexCount == 0 )
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
			vertexbuffer.upload( t.getBuffer() );
			refreshNum = gfxRefresh;

			return this;
		}

		@Override
		public boolean render(final Matrix4f matrix4f)
		{
			if ( vertexbuffer != null )
			{


				GlStateManager.enableClientState( 32884 );
                ARBMultitexture.glActiveTextureARB(GL13.GL_TEXTURE0);
				GlStateManager.enableClientState( 32888 );
                ARBMultitexture.glActiveTextureARB(GL13.GL_TEXTURE1);
				GlStateManager.enableClientState( 32888 );
                ARBMultitexture.glActiveTextureARB(GL13.GL_TEXTURE2);
				GlStateManager.enableClientState( 32886 );

				vertexbuffer.bindBuffer();
				setupArrayPointers();
				vertexbuffer.draw(matrix4f, GL11.GL_QUADS );
                ARBVertexBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, 0);
				GlStateManager.color4f(1f, 1f, 1f, 1f);

				for ( final VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements() )
				{
					final VertexFormatElement.Usage vertexformatelement$enumusage = vertexformatelement.getUsage();
					final int i = vertexformatelement.getIndex();

					switch ( vertexformatelement$enumusage )
					{
						case POSITION:
							GlStateManager.disableClientState( 32884 );
							break;
						case UV:
                            ARBMultitexture.glActiveTextureARB(GL13.GL_TEXTURE0 + i);
							GlStateManager.disableClientState( 32888 );
                            ARBMultitexture.glActiveTextureARB(GL13.GL_TEXTURE0);
							break;
						case COLOR:
							GlStateManager.disableClientState( 32886 );
							GlStateManager.color4f(1f, 1f, 1f ,1f);
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
			GlStateManager.vertexPointer( 3, GL11.GL_FLOAT, 28, 0 );
			GlStateManager.colorPointer( 4, GL11.GL_UNSIGNED_BYTE, 28, 12 );
			GlStateManager.texCoordPointer( 2, GL11.GL_FLOAT, 28, 16 );
            ARBMultitexture.glActiveTextureARB(GL13.GL_TEXTURE1);
			GlStateManager.texCoordPointer( 2, GL11.GL_SHORT, 28, 24 );
            ARBMultitexture.glActiveTextureARB(GL13.GL_TEXTURE0);
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
				vertexbuffer.close();
				vertexbuffer = null;
			}
		}

	}

}
