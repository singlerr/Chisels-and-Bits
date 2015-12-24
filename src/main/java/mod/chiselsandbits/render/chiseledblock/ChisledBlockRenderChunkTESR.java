package mod.chiselsandbits.render.chiseledblock;

import java.util.EnumSet;

import org.lwjgl.opengl.GL11;

import mod.chiselsandbits.chiseledblock.EnumTESRRenderState;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import mod.chiselsandbits.chiseledblock.TileRenderChunk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ChisledBlockRenderChunkTESR extends TileEntitySpecialRenderer<TileEntityBlockChiseledTESR>
{
	private final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();

	@Override
	public void renderTileEntityAt(
			final TileEntityBlockChiseledTESR te,
			final double x,
			final double y,
			final double z,
			final float partialTicks,
			final int destroyStage )
	{
		final EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderPass() == 0 ? MinecraftForgeClient.getRenderLayer() : EnumWorldBlockLayer.TRANSLUCENT;
		final TileRenderChunk trc = te.getRenderChunk();
		final EnumTESRRenderState state = trc.update( layer, 0 );

		if ( trc == null || state == EnumTESRRenderState.SKIP || !trc.isDyanmic )
		{
			return;
		}

		final Tessellator tessellator = Tessellator.getInstance();
		final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
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
		GL11.glPushMatrix();

		if ( Minecraft.isAmbientOcclusionEnabled() )
		{
			GlStateManager.shadeModel( GL11.GL_SMOOTH );
		}
		else
		{
			GlStateManager.shadeModel( GL11.GL_FLAT );
		}

		GL11.glTranslated( -TileEntityRendererDispatcher.staticPlayerX,
				-TileEntityRendererDispatcher.staticPlayerY,
				-TileEntityRendererDispatcher.staticPlayerZ );

		if ( state == EnumTESRRenderState.GENERATE || trc.displayList[layer.ordinal()] == 0 || trc.rebuild[layer.ordinal()] )
		{
			if ( trc.displayList[layer.ordinal()] == 0 )
			{
				trc.displayList[layer.ordinal()] = GLAllocation.generateDisplayLists( 1 );
			}

			GL11.glNewList( trc.displayList[layer.ordinal()], GL11.GL_COMPILE_AND_EXECUTE );
			trc.rebuild[layer.ordinal()] = false;

			worldrenderer.begin( GL11.GL_QUADS, DefaultVertexFormats.BLOCK );
			worldrenderer.setTranslation( 0, 0, 0 );

			final EnumSet<EnumWorldBlockLayer> layers = layer == EnumWorldBlockLayer.TRANSLUCENT ? EnumSet.of( layer ) : EnumSet.complementOf( EnumSet.of( EnumWorldBlockLayer.TRANSLUCENT ) );
			for ( final TileEntityBlockChiseled tx : trc.getTiles() )
			{
				if ( tx instanceof TileEntityBlockChiseledTESR )
				{
					final IExtendedBlockState estate = ( (TileEntityBlockChiseledTESR) tx ).getTileRenderState();

					for ( final EnumWorldBlockLayer lx : layers )
					{
						final ChisledBlockBaked model = ChisledBlockSmartModel.getCachedModel( tx, lx );

						if ( !model.isEmpty() )
						{
							blockRenderer.getBlockModelRenderer().renderModel( tx.getWorld(), model, estate, tx.getPos(), worldrenderer, false );
						}
					}
				}
			}

			tessellator.draw();

			GL11.glEndList();
		}
		else if ( state == EnumTESRRenderState.RENDER )
		{
			GL11.glCallList( trc.displayList[layer.ordinal()] );
		}

		GL11.glPopMatrix();
		RenderHelper.enableStandardItemLighting();
	}

}
