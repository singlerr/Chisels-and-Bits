package mod.chiselsandbits.chiseledblock.serialization;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class BitStream
{
	int offset = 0;
	int bit = 0;
	int lastLiveInt = -1;

	int currentInt = 0;

	IntBuffer output;
	ByteBuffer bytes;

	public BitStream()
	{
		bytes = ByteBuffer.allocate( 250 );
		output = bytes.asIntBuffer();
	}

	private BitStream(
			final ByteBuffer wrap )
	{
		bytes = wrap;
		output = bytes.asIntBuffer();
		currentInt = output.capacity() > 0 ? output.get( 0 ) : 0;
	}

	public static BitStream valueOf(
			final ByteBuffer wrap )
	{
		return new BitStream( wrap );
	}

	public void reset()
	{
		offset = 0;
		bit = 0;
		lastLiveInt = -1;
		output.put( 0, 0 );
		currentInt = 0;
	}

	public byte[] toByteArray()
	{
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		output.put( offset, currentInt );
		o.write( bytes.array(), 0, ( lastLiveInt + 1 ) * 4 );
		return o.toByteArray();
	}

	public boolean get()
	{
		final boolean result = ( currentInt & 1 << bit ) != 0;

		if ( ++bit >= 32 )
		{
			++offset;
			bit = 0;
			currentInt = output.capacity() > offset ? output.get( offset ) : 0;
		}

		return result;
	}

	public void add(
			final boolean b )
	{
		if ( b )
		{
			currentInt = currentInt | 1 << bit;
			lastLiveInt = offset;
		}

		if ( ++bit >= 32 )
		{
			output.put( offset, currentInt );
			++offset;
			bit = 0;
			currentInt = 0;

			// reset?
			if ( output.capacity() <= offset )
			{
				final ByteBuffer ibytes = ByteBuffer.allocate( bytes.limit() + 248 );
				final IntBuffer ioutput = ibytes.asIntBuffer();

				// copy...
				System.arraycopy( bytes.array(), 0, ibytes.array(), 0, bytes.capacity() );

				bytes = ibytes;
				output = ioutput;
			}

			output.put( offset, 0 );
		}
	}

}
