package mod.chiselsandbits.chiseledblock.serialization;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import io.netty.buffer.Unpooled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob.IntegerRef;
import net.minecraft.network.PacketBuffer;

public class BlobSerializer
{

	private final int types;
	private final TIntIntMap index; // deflate...
	private final int[] palette; // inflate...
	private final int bitsPerInt;
	private final int bitsPerIntMinus1;
	private final int offset = 0;

	public BlobSerializer(
			final VoxelBlob toDeflate )
	{
		int offset = 0;
		final HashMap<Integer, IntegerRef> entries = toDeflate.getBlockCounts();

		index = new TIntIntHashMap( types = entries.size() );
		palette = new int[types];

		for ( final Entry<Integer, IntegerRef> block : entries.entrySet() )
		{
			palette[offset] = block.getKey();
			index.put( block.getKey(), offset++ );
		}

		bitsPerInt = bitsPerBit();
		bitsPerIntMinus1 = bitsPerInt - 1;
	}

	public BlobSerializer(
			final PacketBuffer toInflate )
	{
		types = toInflate.readVarIntFromBuffer();
		palette = new int[types];
		index = null;

		for ( int x = 0; x < types; x++ )
		{
			palette[x] = readStateID( toInflate );
		}

		bitsPerInt = bitsPerBit();
		bitsPerIntMinus1 = bitsPerInt - 1;
	}

	public void write(
			final PacketBuffer to )
	{
		// palette size...
		to.writeVarIntToBuffer( palette.length );

		// write palette
		for ( int x = 0; x < palette.length; x++ )
		{
			writeStateID( to, palette[x] );
		}
	}

	protected int readStateID(
			final PacketBuffer buffer )
	{
		return buffer.readVarIntFromBuffer();
	}

	protected void writeStateID(
			final PacketBuffer buffer,
			final int key )
	{
		buffer.writeVarIntToBuffer( key );
	}

	private int bitsPerBit()
	{
		int maxValue = types - 1; // maximum value to represent..
		int bits = 0;

		while ( maxValue > 0 )
		{
			bits++;
			maxValue = maxValue >> 1;
		}

		return bits > 0 ? bits : 1;
	}

	private int getIndex(
			final int stateID )
	{
		return index.get( stateID );
	}

	private int getStateID(
			final int indexID )
	{
		return palette[indexID];
	}

	static ThreadLocal<ByteBuffer> buffer = new ThreadLocal<ByteBuffer>();

	public static ByteBuffer getBuffer()
	{
		ByteBuffer bb = buffer.get();

		if ( bb == null )
		{
			bb = ByteBuffer.allocate( 3145728 );
			buffer.set( bb );
		}

		return bb;
	}

	static ThreadLocal<PacketBuffer> pbuffer = new ThreadLocal<PacketBuffer>();

	public static PacketBuffer getPacketBuffer()
	{
		PacketBuffer bb = pbuffer.get();

		if ( bb == null )
		{
			bb = new PacketBuffer( Unpooled.buffer() );
			pbuffer.set( bb );
		}

		bb.resetReaderIndex();
		bb.resetWriterIndex();

		return bb;
	}

	static ThreadLocal<BitStream> bitbuffer = new ThreadLocal<BitStream>();

	public static BitStream getBitSet()
	{
		BitStream bb = bitbuffer.get();

		if ( bb == null )
		{
			bb = new BitStream();
			bitbuffer.set( bb );
		}

		bb.reset();
		return bb;
	}

	public int readVoxelStateID(
			final BitStream bits )
	{
		int val = 0;

		for ( int x = bitsPerIntMinus1; x >= 0; x-- )
		{
			val |= bits.get() ? 1 << x : 0;
		}

		return getStateID( val );
	}

	public int getVersion()
	{
		return VoxelBlob.VERSION_COMPACT;
	}

	public void writeVoxelState(
			final int stateID,
			final BitStream set )
	{
		final int val = getIndex( stateID );

		switch ( bitsPerInt )
		{
			case 16:
				set.add( ( val & 0x8000 ) != 0 );
			case 15:
				set.add( ( val & 0x4000 ) != 0 );
			case 14:
				set.add( ( val & 0x2000 ) != 0 );
			case 13:
				set.add( ( val & 0x1000 ) != 0 );
			case 12:
				set.add( ( val & 0x800 ) != 0 );
			case 11:
				set.add( ( val & 0x400 ) != 0 );
			case 10:
				set.add( ( val & 0x200 ) != 0 );
			case 9:
				set.add( ( val & 0x100 ) != 0 );
			case 8:
				set.add( ( val & 0x80 ) != 0 );
			case 7:
				set.add( ( val & 0x40 ) != 0 );
			case 6:
				set.add( ( val & 0x20 ) != 0 );
			case 5:
				set.add( ( val & 0x10 ) != 0 );
			case 4:
				set.add( ( val & 0x8 ) != 0 );
			case 3:
				set.add( ( val & 0x4 ) != 0 );
			case 2:
				set.add( ( val & 0x2 ) != 0 );
			case 1:
				set.add( ( val & 0x1 ) != 0 );
		}
	}

}
