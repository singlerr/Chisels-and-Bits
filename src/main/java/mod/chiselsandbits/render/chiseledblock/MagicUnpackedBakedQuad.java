package mod.chiselsandbits.render.chiseledblock;

import java.lang.reflect.Field;

import mezz.jei.util.Log;
import mod.chiselsandbits.render.cache.InMemoryQuadCompressor;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IColoredBakedQuad;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

/**
 * Based on the UnpackedBakedQuad Builder, this is temporary until the issue can
 * be resolved in forge, and 1.8.9 becomes the main version.
 */
public class MagicUnpackedBakedQuad extends UnpackedBakedQuad
{

	private static InMemoryQuadCompressor inMemoryCompressor = new InMemoryQuadCompressor();
	private static Field d;

	{
		for ( final Field f : BakedQuad.class.getDeclaredFields() )
		{
			if ( f.getType() == int[].class )
			{
				d = f;
				d.setAccessible( true );
				break;
			}
		}
	}

	private void setInts(
			final MagicUnpackedBakedQuad quad,
			final int[] vertData )
	{
		try
		{
			d.set( quad, vertData );
		}
		catch ( final IllegalArgumentException e )
		{
			Log.error( "Derp.", e );
		}
		catch ( final IllegalAccessException e )
		{
			Log.error( "Derp.", e );
		}
	}

	@Override
	public int[] getVertexData()
	{
		final VertexFormat format = DefaultVertexFormats.ITEM;

		if ( !packed )
		{
			final int[] vertData = new int[format.getNextOffset() /* / 4 * 4 */];
			setInts( this, vertData );

			packed = true;
			for ( int v = 0; v < 4; v++ )
			{
				for ( int e = 0; e < format.getElementCount(); e++ )
				{
					LightUtil.pack( unpackedData[v][e], vertexData, format, v, e );
				}
			}
		}

		return vertexData;
	}

	public MagicUnpackedBakedQuad(
			final float[][][] unpackedData,
			final int tint,
			final EnumFacing orientation,
			final VertexFormat format )
	{
		super( inMemoryCompressor.compress( unpackedData ), tint, orientation, format );
		setInts( this, null );
	}

	public static class Colored extends MagicUnpackedBakedQuad implements IColoredBakedQuad
	{
		public Colored(
				final float[][][] unpackedData,
				final int tint,
				final EnumFacing orientation,
				final VertexFormat format )
		{
			super( unpackedData, tint, orientation, format );
		}
	}

	public static class Builder implements IVertexConsumer
	{
		private final VertexFormat format;
		private final float[][][] unpackedData;
		private int tint = -1;
		private EnumFacing orientation;
		private boolean isColored = false;

		private int vertices = 0;
		private int elements = 0;
		private boolean full = false;

		public Builder(
				final VertexFormat format )
		{
			this.format = format;
			unpackedData = new float[4][format.getElementCount()][4];
		}

		@Override
		public VertexFormat getVertexFormat()
		{
			return format;
		}

		@Override
		public void setQuadTint(
				final int tint )
		{
			this.tint = tint;
		}

		@Override
		public void setQuadOrientation(
				final EnumFacing orientation )
		{
			this.orientation = orientation;
		}

		@Override
		public void setQuadColored()
		{
			isColored = true;
		}

		@Override
		public void put(
				final int element,
				final float... data )
		{
			for ( int i = 0; i < 4; i++ )
			{
				if ( i < data.length )
				{
					unpackedData[vertices][element][i] = data[i];
				}
				else
				{
					unpackedData[vertices][element][i] = 0;
				}
			}
			elements++;
			if ( elements == format.getElementCount() )
			{
				vertices++;
				elements = 0;
			}
			if ( vertices == 4 )
			{
				full = true;
			}
		}

		public UnpackedBakedQuad build()
		{
			if ( !full )
			{
				throw new IllegalStateException( "not enough data" );
			}
			if ( isColored )
			{
				return new Colored( unpackedData, tint, orientation, format );
			}
			return new MagicUnpackedBakedQuad( unpackedData, tint, orientation, format );
		}
	}
}
