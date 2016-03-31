package mod.chiselsandbits.share;

public class ScreenshotDecoder
{

	public byte[] pixelBytes; // RGBA
	public int pixelOffset;
	public int channel;

	byte readByte()
	{
		int currentValue = pixelBytes[pixelOffset + channel];
		int alpha = pixelBytes[pixelOffset + 3];

		pixelOffset++;

		if ( alpha > 128 )
		{
			int p1 = pixelBytes[pixelOffset + 0];
			int p2 = pixelBytes[pixelOffset + 1];
			int p3 = pixelBytes[pixelOffset + 2];

			currentValue = ( ( alpha & 3 ) << 6 ) | ( ( p1 & 3 ) << 4 ) | ( ( p2 & 3 ) << 2 ) | ( ( p3 & 3 ) );

			pixelOffset += 4;
			channel = 0;
		}
		else if ( channel >= 3 )
		{
			pixelOffset += 4;
			channel = 0;
		}

		return (byte) currentValue;
	}

	byte[] imageDecode(
			byte[] pixelbytes )
	{
		pixelBytes = pixelbytes;
		pixelOffset = 0;
		channel = 0;

		int size = readByte() << 24;
		size |= readByte() << 16;
		size |= readByte() << 8;
		size |= readByte();

		if ( size > pixelbytes.length )
			throw new RuntimeException( "Not enough Data." );

		byte[] out = new byte[size];
		for ( int x = 0; x < size; ++x )
			out[x] = readByte();

		return out;
	}
}
