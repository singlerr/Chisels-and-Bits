package mod.chiselsandbits.share;

import java.io.IOException;

public class ScreenshotDecoder
{

	public byte[] pixelBytes; // RGBA
	public int pixelOffset;
	public int channel = 1;

	byte readByte()
	{
		int currentValue = pixelBytes[pixelOffset + channel] & 0xff;
		final int alpha = pixelBytes[pixelOffset + 0] & 0xff;

		channel++;

		if ( alpha > 128 )
		{
			final int p1 = pixelBytes[pixelOffset + 3] & 0xff;
			final int p2 = pixelBytes[pixelOffset + 2] & 0xff;
			final int p3 = pixelBytes[pixelOffset + 1] & 0xff;

			currentValue = ( alpha & 3 ) << 6 | ( p1 & 3 ) << 4 | ( p2 & 3 ) << 2 | p3 & 3;

			pixelOffset += 4;
			channel = 1;
		}
		else if ( channel >= 3 )
		{
			pixelOffset += 4;
			channel = 1;
		}

		return (byte) currentValue;
	}

	byte[] imageDecode(
			final byte[] pixelbytes ) throws IOException
	{
		pixelBytes = pixelbytes;
		pixelOffset = 0;
		channel = 1;

		int size = readByte() << 24;
		size |= readByte() << 16;
		size |= readByte() << 8;
		size |= readByte();

		if ( size > pixelbytes.length )
		{
			throw new IOException( "Not enough Data." );
		}

		final byte[] out = new byte[size];
		for ( int x = 0; x < size; ++x )
		{
			out[x] = readByte();
		}

		return out;
	}
}
