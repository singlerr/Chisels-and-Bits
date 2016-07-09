package mod.chiselsandbits.share;

import java.nio.ByteBuffer;

import mod.chiselsandbits.chiseledblock.serialization.BitStream;

public class ShareFormatReader
{

	final BitStream inner;

	public ShareFormatReader(
			final byte[] data )
	{
		inner = BitStream.valueOf( 0, ByteBuffer.wrap( data ) );
	}

	public boolean readBool()
	{
		return inner.get();
	}

	public int readBits(
			final int bits )
	{
		return inner.readBits( bits );
	}

	public int readInt()
	{
		boolean hasMoreData = true;

		int value = 0;
		int offset = 0;
		while ( hasMoreData )
		{
			hasMoreData = readBool();
			value = value | readBits( 7 ) << offset;
			offset += 7;
		}

		return value;
	}

	public byte[] readBytes()
	{
		final int size = readInt();
		final byte[] data = new byte[size];

		for ( int x = 0; x < data.length; ++x )
		{
			data[x] = (byte) inner.readBits( 8 );
		}

		return data;
	}

	public void snapToByte()
	{
		inner.snapToByte();
	}

	public int consumedBytes()
	{
		return inner.consumedBytes();
	}

}
