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

	private static int SEVEN_BIT_FILL = 0x7F;
	private static int INVERT_SEVEN_BIT_FILL = ~SEVEN_BIT_FILL;

	public void writeInt(
			int value )
	{
		while ( ( value & INVERT_SEVEN_BIT_FILL ) != 0 )
		{
			final int sevenBits = value & 0x7F;

			writeBool( true );// means this value has more data...
			writeBits( sevenBits, 7 ); // write the 7.

			value = value >>> 7; // remove 7 bits and continue.
		}

		writeBool( false );// means this value has more data...
		writeBits( value, 7 ); // write the last 7 with a 0 indicating done.
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

	public void snapToByte()
	{
		inner.snapToByte();
	}

}
