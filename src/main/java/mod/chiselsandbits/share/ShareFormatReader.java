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
		boolean repeat = readBool();
		int myInt = readBits( 7 );
		int limit = 4;

		int offset = 7;
		while ( repeat && --limit >= 0 )
		{
			repeat = readBool();
			myInt |= readBits( 7 ) << offset;
			offset += 7;
		}

		return myInt;
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
		inner.readSnapToByte();
	}

	public int consumedBytes()
	{
		return inner.consumedBytes();
	}

}
