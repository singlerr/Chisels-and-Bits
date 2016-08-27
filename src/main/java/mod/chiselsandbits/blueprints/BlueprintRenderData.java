package mod.chiselsandbits.blueprints;

import java.util.EnumSet;

import org.lwjgl.opengl.GL11;

import mod.chiselsandbits.blueprints.BlueprintData.EnumLoadState;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.client.RenderHelper;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.render.chiseledblock.ChiselLayer;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBaked;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import mod.chiselsandbits.render.chiseledblock.tesr.ChisledBlockRenderChunkTESR;
import mod.chiselsandbits.render.helpers.DeleteDisplayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BlueprintRenderData implements Runnable
{

	Tessellator t; // when this is filled we can upload it to a new displayList
	int displayList = 0;
	BlueprintData data;

	public void render()
	{
		if ( t != null )
		{
			displayList = GLAllocation.generateDisplayLists( 1 );
			try
			{
				GL11.glNewList( displayList, GL11.GL_COMPILE );
				t.draw();
			}
			catch ( final IllegalStateException e )
			{
				Log.logError( "Erratic Tessellator Behavior", e );
			}
			finally
			{
				GL11.glEndList();
				t = null;
			}
		}

		if ( displayList != 0 )
		{
			GL11.glCallList( displayList );
		}
	}

	public BlueprintRenderData(
			final BlueprintData data )
	{
		if ( data.getState() == EnumLoadState.LOADED )
		{
			this.data = data;
			final Thread t = new Thread( this );
			t.setName( "Blueprint Model Generation Thread" );
			t.start();
		}
	}

	@Override
	public void run()
	{
		final Tessellator tessellator = new Tessellator( 0 );

		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();

		final VertexBuffer worldrenderer = tessellator.getBuffer();
		final BlockPos chunkOffset = BlockPos.ORIGIN;

		try
		{
			worldrenderer.begin( GL11.GL_QUADS, DefaultVertexFormats.ITEM );
			worldrenderer.setTranslation( -chunkOffset.getX(), -chunkOffset.getY(), -chunkOffset.getZ() );
		}
		catch ( final IllegalStateException e )
		{
			Log.logError( "Invalid Tessellator Behavior", e );
		}

		final EnumSet<ChiselLayer> layers = EnumSet.allOf( ChiselLayer.class );
		final BlueprintRenderWorld rw = new BlueprintRenderWorld( data );

		for ( final BlockPos pos : BlockPos.getAllInBoxMutable( BlockPos.ORIGIN, new BlockPos( data.getXSize(), data.getYSize(), data.getZSize() ) ) )
		{
			final TileEntityBlockChiseled tx = rw.getChisledEntity( pos );
			worldrenderer.setTranslation( pos.getX(), pos.getY(), pos.getZ() );

			if ( tx != null )
			{
				final IExtendedBlockState estate = tx.getRenderState( null );
				for ( final ChiselLayer lx : layers )
				{
					final ChiseledBlockBaked model = ChiseledBlockSmartModel.getCachedModel( tx, lx, DefaultVertexFormats.ITEM );

					if ( !model.isEmpty() )
					{
						RenderHelper.uploadBlockModel( worldrenderer, model, rw, pos, 200 );
					}
				}
			}
			else
			{
				IBlockState state = rw.getBlockState( pos );
				if ( state.getBlock() != Blocks.AIR )
				{
					state = state.getActualState( rw, pos );
					final IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState( state );
					if ( model != null )
					{
						RenderHelper.uploadBlockModel( worldrenderer, model, rw, pos, 200 );
					}
				}
			}
		}

		t = tessellator;
	}

	@Override
	protected void finalize() throws Throwable
	{
		if ( displayList != 0 )
		{
			ChisledBlockRenderChunkTESR.addNextFrameTask( new DeleteDisplayList( displayList ) );
		}
	}

}
