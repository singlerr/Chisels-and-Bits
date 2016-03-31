package mod.chiselsandbits.share;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.Log;

public class ShareWorldData
{

	public static class SharedWorldBlock
	{
		public SharedWorldBlock(
				byte[] bytes )
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
				catch ( IOException e )
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

	public final int xSize;
	public final int ySize;
	public final int zSize;

	int[] blocks;
	SharedWorldBlock[] models;

	ShareWorldData(
			BufferedImage img )
	{
		byte[] data = new byte[img.getWidth() * img.getHeight() * 4];

		ScreenshotDecoder sdecoder = new ScreenshotDecoder();
		byte[] compressed = sdecoder.imageDecode( data );
		byte[] uncompressed = null;

		try
		{
			final InflaterInputStream in = new InflaterInputStream( new ByteArrayInputStream( compressed ) );
			ByteArrayOutputStream bout = new ByteArrayOutputStream( compressed.length );

			int b;
			while ( ( b = in.read() ) != -1 )
			{
				bout.write( b );
			}
			bout.close();
			in.close();

			uncompressed = bout.toByteArray();
		}
		catch ( final IOException e )
		{
			Log.logError( "Error Deflating Data", e );
		}

		ShareFormatReader reader = new ShareFormatReader( uncompressed );

		int format = reader.readInt();

		if ( format != 1 )
		{
			throw new RuntimeException( "Invalid format!" );
		}

		xSize = reader.readInt();
		ySize = reader.readInt();
		zSize = reader.readInt();

		int bits = reader.readInt();

		blocks = new int[xSize * ySize * zSize];
		for ( int x = 0; x < blocks.length; x++ )
		{
			blocks[x] = reader.readBits( bits );
		}

		int modelCount = reader.readInt();
		models = new SharedWorldBlock[modelCount];
		for ( int x = 0; x < models.length; x++ )
		{
			models[x] = new SharedWorldBlock( reader.readBytes() );
		}
	}

}
