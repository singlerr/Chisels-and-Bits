package mod.chiselsandbits.render.chiseledblock.tesr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;

public class TileList implements Iterable<TileEntityBlockChiseledTESR>
{

	private static class MWR<T> extends WeakReference<TileEntityBlockChiseledTESR>
	{

		public MWR(
				final TileEntityBlockChiseledTESR referent )
		{
			super( referent );
		}

		@Override
		public int hashCode()
		{
			final TileEntityBlockChiseledTESR h = get();

			if ( h == null )
			{
				return super.hashCode();
			}

			return h.hashCode();
		}

		@Override
		public boolean equals(
				final Object obj )
		{
			if ( this == obj )
			{
				return true;
			}

			final TileEntityBlockChiseledTESR o = get();
			if ( o != null )
			{
				final Object b = obj instanceof WeakReference ? ( (WeakReference<?>) obj ).get() : obj;

				if ( b instanceof TileEntityBlockChiseledTESR )
				{
					return ( (TileEntityBlockChiseledTESR) b ).getPos().equals( o.getPos() );
				}

				return o == b;
			}

			return false;
		}

	};

	private final ArrayList<WeakReference<TileEntityBlockChiseledTESR>> tiles = new ArrayList<WeakReference<TileEntityBlockChiseledTESR>>();

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	public Lock getReadLock()
	{
		return r;
	}

	public Lock getWriteLock()
	{
		return w;
	}

	@Override
	public Iterator<TileEntityBlockChiseledTESR> iterator()
	{
		final Iterator<WeakReference<TileEntityBlockChiseledTESR>> o = tiles.iterator();

		return new Iterator<TileEntityBlockChiseledTESR>() {

			TileEntityBlockChiseledTESR which;

			@Override
			public TileEntityBlockChiseledTESR next()
			{
				return which;
			}

			@Override
			public void remove()
			{
				// nope!
			}

			@Override
			public boolean hasNext()
			{
				while ( o.hasNext() )
				{
					final WeakReference<TileEntityBlockChiseledTESR> w = o.next();
					final TileEntityBlockChiseledTESR t = w.get();

					if ( t != null )
					{
						which = t;
						return true;
					}
					else
					{
						ChisledBlockRenderChunkTESR.addNextFrameTask( new Runnable() {

							@Override
							public void run()
							{
								getWriteLock().lock();
								try
								{
									tiles.remove( w );
								}
								finally
								{
									getWriteLock().unlock();
								}
							}

						} );
					}
				}

				return false;
			}
		};
	}

	public void add(
			final TileEntityBlockChiseledTESR which )
	{
		tiles.add( new MWR<TileEntityBlockChiseledTESR>( which ) );
	}

	public boolean isEmpty()
	{
		return !iterator().hasNext();
	}

	public void remove(
			final TileEntityBlockChiseledTESR which )
	{
		tiles.remove( new MWR<TileEntityBlockChiseledTESR>( which ) );
	}

	public List<TileEntityBlockChiseledTESR> createCopy()
	{
		final ArrayList<TileEntityBlockChiseledTESR> t = new ArrayList<TileEntityBlockChiseledTESR>( tiles.size() );

		getReadLock().lock();
		try
		{
			for ( final TileEntityBlockChiseledTESR x : this )
			{
				t.add( x );
			}

			return t;
		}
		finally
		{
			getReadLock().unlock();
		}
	}
}
