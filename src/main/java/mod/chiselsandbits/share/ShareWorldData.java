package mod.chiselsandbits.share;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;

public class ShareWorldData
{

	public static class SharedWorldBlock
	{
		public SharedWorldBlock(
				final byte[] bytes )
		{
			if ( bytes[0] == 1 ) // block
			{
				blob = null;
				isBlob = false;
				blockName = new String( bytes, 1, bytes.length - 1 );
			}
			else if ( bytes[1] == 2 ) // blob )
			{
				blob = new VoxelBlob();
				isBlob = true;
				blockName = null;

				try
				{
					blob.blobFromBytes( bytes, 1, bytes.length - 1 );
				}
				catch ( final IOException e )
				{
					// ; _ ;
				}
			}
			else
			{
				isBlob = false;
				blob = null;
				blockName = "minecraft:air";
			}
		}

		final boolean isBlob;

		final String blockName;
		final VoxelBlob blob;
	};

	private int xSize;
	private int ySize;
	private int zSize;

	int[] blocks;
	SharedWorldBlock[] models;

	public int getXSize()
	{
		return xSize;
	}

	public int getYSize()
	{
		return ySize;
	}

	public int getZSize()
	{
		return zSize;
	}

	public ShareWorldData(
			String data ) throws IOException
	{
		final String header = "[C&B](";
		final String footer = ")[C&B]";

		int start = data.indexOf( header );
		final int end = data.indexOf( footer );

		if ( start == -1 || end == -1 )
		{
			throw new IOException( "Unable to locate C&B Data." );
		}

		start += header.length();
		data = data.substring( start, end );
		final byte[] compressed = Base64.getDecoder().decode( data );
		readCompressed( compressed );
	}

	public ShareWorldData(
			final BufferedImage img ) throws IOException
	{
		final byte[] data = new byte[img.getWidth() * img.getHeight() * 4];

		final ScreenshotDecoder sdecoder = new ScreenshotDecoder();
		final byte[] compressed = sdecoder.imageDecode( data );

		readCompressed( compressed );
	}

	private void readCompressed(
			final byte[] compressed ) throws IOException
	{
		byte[] uncompressed = null;

		final InflaterInputStream in = new InflaterInputStream( new ByteArrayInputStream( compressed ) );
		final ByteArrayOutputStream bout = new ByteArrayOutputStream( compressed.length );

		int b;
		while ( ( b = in.read() ) != -1 )
		{
			bout.write( b );
		}
		bout.close();
		in.close();

		uncompressed = bout.toByteArray();

		final ShareFormatReader reader = new ShareFormatReader( uncompressed );

		final int format = reader.readInt();

		if ( format != 1 )
		{
			throw new IOException( "Invalid format!" );
		}

		xSize = reader.readInt();
		ySize = reader.readInt();
		zSize = reader.readInt();

		final int bits = reader.readInt();

		blocks = new int[xSize * ySize * zSize];
		for ( int x = 0; x < blocks.length; x++ )
		{
			blocks[x] = reader.readBits( bits );
		}

		final int modelCount = reader.readInt();
		models = new SharedWorldBlock[modelCount];
		for ( int x = 0; x < models.length; x++ )
		{
			models[x] = new SharedWorldBlock( reader.readBytes() );
		}

		structureData = new byte[reader.consumedBytes()];
		System.arraycopy( uncompressed, 0, structureData, 0, structureData.length );
	}

	private byte[] structureData;

	public byte[] getStuctureData() throws IOException
	{
		final ByteArrayOutputStream byteStream = new ByteArrayOutputStream( structureData.length );

		try
		{
			final DeflaterOutputStream zipStream = new DeflaterOutputStream( byteStream );
			try
			{
				zipStream.write( structureData );
			}
			finally
			{
				zipStream.close();
			}
		}
		finally
		{
			byteStream.close();
		}

		return byteStream.toByteArray();
	}

}
