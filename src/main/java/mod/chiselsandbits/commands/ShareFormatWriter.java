package mod.chiselsandbits.commands;

import mod.chiselsandbits.chiseledblock.serialization.BitStream;

public class ShareFormatWriter
{

	BitStream inner = new BitStream();

	public void writeBool(
			final boolean what )
	{
		inner.add( what );
	}

	public void writeBits(
			final int value,
			final int bits )
	{
		inner.writeBits( value, bits );
	}

	public void writeInt(
			int value )
	{
		while ( value > 0x7F )
		{
			final int sevenBits = value & 0x7F;

			writeBool( true );// means this value has more data...
			writeBits( sevenBits, 7 ); // write the 7.

			value = value >> 7; // remove 7 bits and continue.
		}

		writeBits( value, 8 ); // write the last 7 with a 0 indicating done.
	}

	public void writeBytes(
			final byte[] data )
	{
		writeInt( data.length );
		for ( int x = 0; x < data.length; ++x )
		{
			inner.writeBits( data[x], 8 );
		}
	}

}
