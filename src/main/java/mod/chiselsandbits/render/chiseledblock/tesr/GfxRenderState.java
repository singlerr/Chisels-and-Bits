package mod.chiselsandbits.render.chiseledblock.tesr;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.*;

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
		return true;
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
				vertexbuffer.bindBuffer();
                Minecraft.getInstance().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
				DefaultVertexFormats.BLOCK.setupBufferState(0L);
                vertexbuffer.draw(matrix4f, GL11.GL_QUADS );
                VertexBuffer.unbindBuffer();
                RenderSystem.clearCurrentColor();
                DefaultVertexFormats.BLOCK.clearBufferState();

				return true;
			}

			return false;
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
