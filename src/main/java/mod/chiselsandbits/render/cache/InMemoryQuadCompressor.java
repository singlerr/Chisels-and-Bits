package mod.chiselsandbits.render.cache;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

import mod.chiselsandbits.render.cache.CacheMap.EqTest;

public class InMemoryQuadCompressor implements Runnable
{

	protected static final float EPSILON = 0.0001f;

	private static CacheMap<float[][], WeakReference<float[][]>> cachelvl2 = new CacheMap<float[][], WeakReference<float[][]>>( new EqTest() {

		@Override
		public boolean doTest(
				final Object a,
				final Object b )
		{
			final float[][] aa = (float[][]) a;
			final float[][] bb = (float[][]) b;

			if ( aa.length != bb.length )
			{
				return false;
			}

			for ( int x = 0; x < aa.length; x++ )
			{
				if ( aa[x] != bb[x] )
				{
					return false;
				}
			}

			return true;
		}

		@Override
		public int getHash(
				final Object referent )
		{
			final float[][] a = (float[][]) referent;
			int out = 0;

			for ( int x = 0; x < a.length; x++ )
			{
				out ^= System.identityHashCode( a[x] ) << x;
			}

			return out;
		}

	} );

	private static CacheMap<float[], WeakReference<float[]>> cache = new CacheMap<float[], WeakReference<float[]>>( new EqTest() {

		@Override
		public boolean doTest(
				final Object a,
				final Object b )
		{
			final float[] aa = (float[]) a;
			final float[] bb = (float[]) b;

			if ( aa.length != bb.length )
			{
				return false;
			}

			for ( int x = 0; x < aa.length; x++ )
			{
				if ( Math.abs( aa[x] - bb[x] ) > EPSILON )
				{
					return false;
				}
			}

			return true;
		}

		@Override
		public int getHash(
				final Object referent )
		{
			final float[] a = (float[]) referent;
			int out = 0;

			for ( int x = 0; x < a.length; x++ )
			{
				out ^= (int) ( a[x] * 100 ) << x;
			}

			return out;
		}

	} );

	Queue<WeakReference<float[][][]>> submissions = new LinkedList<WeakReference<float[][][]>>();

	float[] junk;

	private float[] referize(
			final float[] fs )
	{
		final WeakReference<float[]> o = cache.get( fs );

		if ( o != null )
		{
			final float[] f = o.get();
			if ( f != null )
			{
				return f;
			}
		}

		cache.put( fs, new WeakReference<float[]>( fs ) );
		return fs;
	}

	private float[][] referizelvl2(
			final float[][] fs )
	{
		final WeakReference<float[][]> o = cachelvl2.get( fs );

		if ( o != null )
		{
			final float[][] f = o.get();
			if ( f != null )
			{
				return f;
			}
		}

		cachelvl2.put( fs, new WeakReference<float[][]>( fs ) );
		return fs;
	}

	public InMemoryQuadCompressor()
	{
		final Thread t = new Thread( this );
		t.setName( "C&B In Memory Compression" );
		t.setPriority( Thread.MIN_PRIORITY );
		t.start();
	}

	public float[][][] compress(
			final float[][][] unpackedData )
	{
		synchronized ( submissions )
		{
			submissions.add( new WeakReference<float[][][]>( unpackedData ) );
		}

		return unpackedData;
	}

	@Override
	public void run()
	{
		while ( true )
		{
			try
			{
				Thread.sleep( 5 );
			}
			catch ( final InterruptedException e )
			{
				// :P
			}

			do
			{
				float[][][] active = null;
				WeakReference<float[][][]> l;

				synchronized ( submissions )
				{
					l = submissions.poll();
				}

				if ( l == null )
				{
					break;
				}

				active = l.get();

				if ( active == null )
				{
					continue;
				}

				for ( int x = 0; x < active.length; x++ )
				{
					for ( int y = 0; y < active[x].length; y++ )
					{
						active[x][y] = referize( active[x][y] );
					}

					active[x] = referizelvl2( active[x] );
				}
			}
			while ( true );
		}
	}

}
