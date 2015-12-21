package mod.chiselsandbits.chiseledblock.data;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class VoxelBlobStateReference implements Comparable<VoxelBlobStateReference>
{

	private static WeakHashMap<VoxelBlobStateInstance, WeakReference<VoxelBlobStateInstance>> refs = new WeakHashMap<VoxelBlobStateInstance, WeakReference<VoxelBlobStateInstance>>();

	private static VoxelBlobStateInstance FindRef(
			final byte[] v )
	{
		final VoxelBlobStateInstance t = new VoxelBlobStateInstance( v );
		VoxelBlobStateInstance ref = null;

		final WeakReference<VoxelBlobStateInstance> original = refs.get( t );
		if ( original != null )
		{
			ref = original.get();
		}

		if ( ref == null )
		{
			ref = t;
			refs.put( t, new WeakReference<VoxelBlobStateInstance>( t ) );
		}

		return ref;
	}

	private final VoxelBlobStateInstance data;
	public final long weight;

	public byte[] getByteArray()
	{
		return data.v;
	}

	public VoxelBlob getVoxelBlob()
	{
		return data.getBlob();
	}

	public VoxelBlobStateReference(
			final VoxelBlob blob,
			final long weight )
	{
		this( blob.toByteArray(), weight );
	}

	public VoxelBlobStateReference(
			final byte[] v,
			final long weight )
	{
		data = FindRef( v );
		this.weight = weight;
	}

	@Override
	public boolean equals(
			final Object obj )
	{
		if ( !( obj instanceof VoxelBlobStateReference ) )
		{
			return false;
		}

		final VoxelBlobStateReference second = (VoxelBlobStateReference) obj;
		return data.equals( second.data ) && second.weight == weight;
	}

	@Override
	public int hashCode()
	{
		return data.hash ^ (int) ( weight ^ weight >>> 32 );
	}

	@Override
	public int compareTo(
			final VoxelBlobStateReference o )
	{
		final int comp = data.compareTo( o.data );
		if ( comp == 0 )
		{
			if ( weight == o.weight )
			{
				return 0;
			}

			return weight < o.weight ? -1 : 1;
		}
		return comp;
	}

}
