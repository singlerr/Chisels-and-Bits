
package mod.chiselsandbits.chiseledblock.data;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import mod.chiselsandbits.ChiselsAndBits;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;


public class VoxelBlobState implements Comparable<VoxelBlobState>
{

	private static WeakHashMap<VoxelBlobStateRef, WeakReference<VoxelBlobStateRef>> refs = new WeakHashMap<VoxelBlobStateRef, WeakReference<VoxelBlobStateRef>>();

	private static VoxelBlobStateRef FindRef(
			final byte[] v )
	{
		final VoxelBlobStateRef t = new VoxelBlobStateRef( v );
		VoxelBlobStateRef ref = null;

		final WeakReference<VoxelBlobStateRef> original = refs.get( t );
		if ( original != null )
		{
			ref = original.get();
		}

		if ( ref == null )
		{
			ref = t;
			refs.put( t, new WeakReference<VoxelBlobStateRef>( t ) );
		}

		return ref;
	}

	private final VoxelBlobStateRef data;
	public final long weight;

	public byte[] getByteArray()
	{
		return data.v;
	}

	public VoxelBlobState(
			final VoxelBlob blob,
			final long weight )
	{
		this( blob.toByteArray(), weight );
	}

	public VoxelBlobState(
			final VoxelBlobState blob,
			final long weight )
	{
		data = blob.data;
		this.weight = ChiselsAndBits.instance.config.allowBlockAlternatives || FMLCommonHandler.instance().getSide() == Side.SERVER ? 0L : weight;
	}

	public VoxelBlobState(
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
		if ( !( obj instanceof VoxelBlobState ) )
			return false;

		final VoxelBlobState second = ( VoxelBlobState ) obj;
		return data.equals( second.data ) && second.weight == weight;
	}

	@Override
	public int hashCode()
	{
		return data.hash ^ Long.hashCode( weight );
	}

	@Override
	public int compareTo(
			final VoxelBlobState o )
	{
		final int comp = data.compareTo( o.data );
		if ( comp == 0 )
			return Long.compare( weight, o.weight );
		return comp;
	}
}
