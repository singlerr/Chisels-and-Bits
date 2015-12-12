
package mod.chiselsandbits.chiseledblock.data;

import java.util.Arrays;

public class VoxelBlobStateRef implements Comparable<VoxelBlobStateRef>
{

	public final int hash;
	public final byte[] v;

	public VoxelBlobStateRef(
			final byte[] data )
	{
		v = data;
		hash = Arrays.hashCode( v );
	}

	@Override
	public boolean equals(
			final Object obj )
	{
		return compareTo( (VoxelBlobStateRef) obj ) == 0;
	}

	@Override
	public int hashCode()
	{
		return hash;
	}

	@Override
	public int compareTo(
			final VoxelBlobStateRef o )
	{
		if ( o == null )
		{
			return -1;
		}

		int r = Integer.compare( hash, o.hash );

		// length?
		if ( r == 0 )
		{
			r = v.length - o.v.length;
		}

		// for real then...
		if ( r == 0 )
		{
			for ( int x = 0; x < v.length && r == 0; x++ )
			{
				r = v[x] - o.v[x];
			}
		}

		return r;
	}
}
