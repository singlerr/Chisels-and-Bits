package mod.chiselsandbits.chiseledblock.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class VoxelBlob
{

	public final static int dim = 16;
	public final static int dim2 = dim * dim;
	public final static int full_size = dim2 * dim;

	public final static int dim_minus_one = dim - 1;

	private final static int array_size = full_size;

	private final int[] values = new int[array_size];

	public int detail = dim;

	public VoxelBlob()
	{
	}

	public VoxelBlob(
			final VoxelBlob vb )
	{
		for ( int x = 0; x < values.length; ++x )
		{
			values[x] = vb.values[x];
		}
	}

	public boolean canMerge(
			final VoxelBlob second )
	{
		final int sv[] = second.values;

		for ( int x = 0; x < values.length; ++x )
		{
			if ( values[x] != 0 && sv[x] != 0 )
			{
				return false;
			}
		}

		return true;
	}

	public VoxelBlob merge(
			final VoxelBlob second )
	{
		final VoxelBlob out = new VoxelBlob();

		final int secondValues[] = second.values;
		final int ov[] = out.values;

		for ( int x = 0; x < values.length; ++x )
		{
			final int firstValue = values[x];
			ov[x] = firstValue == 0 ? secondValues[x] : firstValue;
		}

		return out;
	}

	public BlockPos getCenter()
	{
		boolean found = false;
		int min_x = 0, min_y = 0, min_z = 0;
		int max_x = 0, max_y = 0, max_z = 0;

		final BitIterator bi = new BitIterator();
		while ( bi.hasNext() )
		{
			if ( bi.getNext( this ) != 0 )
			{
				if ( found )
				{
					min_x = Math.min( min_x, bi.x );
					min_y = Math.min( min_y, bi.y );
					min_z = Math.min( min_z, bi.z );

					max_x = Math.max( max_x, bi.x );
					max_y = Math.max( max_y, bi.y );
					max_z = Math.max( max_z, bi.z );
				}
				else
				{
					found = true;

					min_x = bi.x;
					min_y = bi.y;
					min_z = bi.z;

					max_x = bi.x;
					max_y = bi.y;
					max_z = bi.z;
				}
			}
		}

		return found ? new BlockPos( ( min_x + max_x ) / 2, ( min_y + max_y ) / 2, ( min_z + max_z ) / 2 ) : null;
	}

	public IntegerBox getBounds()
	{
		boolean found = false;
		int min_x = 0, min_y = 0, min_z = 0;
		int max_x = 0, max_y = 0, max_z = 0;

		final BitIterator bi = new BitIterator();
		while ( bi.hasNext() )
		{
			if ( bi.getNext( this ) != 0 )
			{
				if ( found )
				{
					min_x = Math.min( min_x, bi.x );
					min_y = Math.min( min_y, bi.y );
					min_z = Math.min( min_z, bi.z );

					max_x = Math.max( max_x, bi.x + 1 );
					max_y = Math.max( max_y, bi.y + 1 );
					max_z = Math.max( max_z, bi.z + 1 );
				}
				else
				{
					found = true;

					min_x = bi.x;
					min_y = bi.y;
					min_z = bi.z;

					max_x = bi.x + 1;
					max_y = bi.y + 1;
					max_z = bi.z + 1;
				}
			}
		}

		return found ? new IntegerBox( min_x, min_y, min_z, max_x, max_y, max_z ) : null;
	}

	public VoxelBlob flip(
			final EnumAxis axis )
	{
		final VoxelBlob d = new VoxelBlob();

		final BitIterator bi = new BitIterator();
		while ( bi.hasNext() )
		{
			switch ( axis )
			{
				case X:
					d.set( dim_minus_one - bi.x, bi.y, bi.z, bi.getNext( this ) );
					break;
				case Y:
					d.set( bi.x, dim_minus_one - bi.y, bi.z, bi.getNext( this ) );
					break;
				case Z:
					d.set( bi.x, bi.y, dim_minus_one - bi.z, bi.getNext( this ) );
				default:
					throw new NullPointerException();
			}
		}

		return d;
	}

	public VoxelBlob spin(
			final Axis axis )
	{
		final VoxelBlob d = new VoxelBlob();

		/*
		 * Rotate by -90 Degrees: x' = y y' = - x
		 */

		final BitIterator bi = new BitIterator();
		while ( bi.hasNext() )
		{
			switch ( axis )
			{
				case X:
					d.set( bi.x, dim_minus_one - bi.z, bi.y, bi.getNext( this ) );
					break;
				case Y:
					d.set( bi.z, bi.y, dim_minus_one - bi.x, bi.getNext( this ) );
					break;
				case Z:
					d.set( dim_minus_one - bi.y, bi.x, bi.z, bi.getNext( this ) );
					break;
				default:
					throw new NullPointerException();
			}
		}

		return d;
	}

	public void fill(
			final int value )
	{
		for ( int x = 0; x < array_size; x++ )
		{
			values[x] = value;
		}
	}

	public void clear()
	{
		fill( 0 );
	}

	public int air()
	{
		int p = 0;

		for ( int x = 0; x < array_size; x++ )
		{
			if ( values[x] == 0 )
			{
				p++;
			}
		}

		return p;
	}

	public void binaryReplacement(
			final int airReplacement,
			final int solidReplacement )
	{
		for ( int x = 0; x < array_size; x++ )
		{
			values[x] = values[x] == 0 ? airReplacement : solidReplacement;
		}
	}

	public int light()
	{
		int p = 0;

		final HashMap<Integer, IntegerRef> count = new HashMap<Integer, IntegerRef>();

		for ( int x = 0; x < array_size; x++ )
		{
			final int ref = values[x];
			IntegerRef tf = count.get( ref );

			if ( tf == null )
			{
				final IBlockState state = Block.getStateById( ref );
				int l = 0;

				if ( state != null )
				{
					l = state.getBlock().getLightValue();
				}

				count.put( ref, tf = new IntegerRef( ref, l ) );
			}

			p += tf.total;
		}

		return p;
	}

	public int solid()
	{
		int p = 0;

		for ( int x = 0; x < array_size; x++ )
		{
			if ( values[x] != 0 )
			{
				p++;
			}
		}

		return p;
	}

	protected int getBit(
			final int offset )
	{
		return values[offset];
	}

	private void putBit(
			final int offset,
			final int newValue )
	{
		values[offset] = newValue;
	}

	public int get(
			final int x,
			final int y,
			final int z )
	{
		return getBit( x | y << 4 | z << 8 );
	}

	public void set(
			final int x,
			final int y,
			final int z,
			final int value )
	{
		putBit( x | y << 4 | z << 8, value );
	}

	public void clear(
			final int x,
			final int y,
			final int z )
	{
		putBit( x | y << 4 | z << 8, 0 );
	}

	static final int SHORT_BYTES = Short.SIZE / 8;

	public void read(
			final ByteArrayInputStream o ) throws IOException
	{
		final GZIPInputStream w = new GZIPInputStream( o );
		final DataInputStream dio = new DataInputStream( w );

		for ( int x = 0; x < array_size; x++ )
		{
			values[x] = dio.readShort();
		}

		w.close();
	}

	public void write(
			final ByteArrayOutputStream o )
	{
		try
		{
			final GZIPOutputStream w = new GZIPOutputStream( o );
			final DataOutputStream dio = new DataOutputStream( w );

			for ( int x = 0; x < array_size; x++ )
			{
				dio.writeShort( values[x] );
			}

			dio.close();

			w.finish();
			w.close();

			o.close();
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	public byte[] toByteArray()
	{
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		write( out );
		return out.toByteArray();
	}

	public void fromByteArray(
			final byte[] i ) throws IOException
	{
		final ByteArrayInputStream out = new ByteArrayInputStream( i );
		read( out );
	}

	public int getSafe(
			final int x,
			final int y,
			final int z )
	{
		if ( x >= 0 && x < dim && y >= 0 && y < dim && z >= 0 && z < dim )
		{
			return get( x, y, z );
		}

		return 0;
	}

	public static class VisibleFace
	{
		public boolean isEdge;
		public boolean visibleFace;
		public int state;
	};

	public void visibleFace(
			final EnumFacing face,
			int x,
			int y,
			int z,
			final VisibleFace dest,
			final VoxelBlob secondBlob )
	{
		final int solid = get( x, y, z );
		dest.state = solid;

		x += face.getFrontOffsetX();
		y += face.getFrontOffsetY();
		z += face.getFrontOffsetZ();

		if ( x >= 0 && x < dim && y >= 0 && y < dim && z >= 0 && z < dim )
		{
			dest.isEdge = false;
			dest.visibleFace = solid != 0 && get( x, y, z ) == 0;
		}
		else if ( secondBlob != null )
		{
			dest.isEdge = true;
			dest.visibleFace = solid != 0 && secondBlob.get( x - face.getFrontOffsetX() * dim, y - face.getFrontOffsetY() * dim, z - face.getFrontOffsetZ() * dim ) == 0;
		}
		else
		{
			dest.isEdge = true;
			dest.visibleFace = solid != 0;
		}
	}

	public static class IntegerRef
	{
		public final int ref;
		public int total;

		public IntegerRef(
				final int ref,
				final int total )
		{
			this.ref = ref;
			this.total = total;
		}
	};

	public static class CommonBlock
	{
		public int ref;
		public boolean isFull;
	};

	public HashMap<Integer, IntegerRef> getBlockCounts()
	{
		final HashMap<Integer, IntegerRef> count = new HashMap<Integer, IntegerRef>();

		for ( int x = 0; x < array_size; x++ )
		{
			final int ref = values[x];

			final IntegerRef tf = count.get( ref );
			if ( tf == null )
			{
				count.put( ref, new IntegerRef( ref, 1 ) );
			}
			else
			{
				tf.total++;
			}
		}

		return count;
	}

	public CommonBlock mostCommonBlock()
	{
		final HashMap<Integer, IntegerRef> count = getBlockCounts();

		IntegerRef out = new IntegerRef( 0, 0 );

		for ( final IntegerRef r : count.values() )
		{
			if ( r.total > out.total && r.ref != 0 )
			{
				out = r;
			}
		}

		final CommonBlock cb = new CommonBlock();
		cb.ref = out.ref;
		cb.isFull = out.total == array_size;
		return cb;
	}

	public float getLight()
	{
		// 15 is the getLightValue range, 100 is to convert the percentage.
		final float light_size = ChiselsAndBits.instance.config.bitLightPercentage * array_size * 15.0f / 100.0f;
		final float o = light() / light_size;
		return o;
	}

	public float getOpacity()
	{
		final float o = (float) solid() / (float) array_size;
		return o;
	}

	public VoxelBlob offset(
			final int xx,
			final int yy,
			final int zz )
	{
		final VoxelBlob out = new VoxelBlob();

		for ( int z = 0; z < dim; z++ )
		{
			for ( int y = 0; y < dim; y++ )
			{
				for ( int x = 0; x < dim; x++ )
				{
					out.set( x, y, z, getSafe( x - xx, y - yy, z - zz ) );
				}
			}
		}

		return out;
	}

	@SideOnly( Side.CLIENT )
	public void listContents(
			final List<String> details )
	{
		final HashMap<Integer, Integer> states = new HashMap<Integer, Integer>();
		final HashMap<String, Integer> contents = new HashMap<String, Integer>();

		final BitIterator bi = new BitIterator();
		while ( bi.hasNext() )
		{
			final int state = bi.getNext( this );
			if ( state == 0 )
			{
				continue;
			}

			Integer count = states.get( state );

			if ( count == null )
			{
				count = 1;
			}
			else
			{
				count++;
			}

			states.put( state, count );
		}

		for ( final Entry<Integer, Integer> e : states.entrySet() )
		{
			final IBlockState state = Block.getStateById( e.getKey() );
			if ( state == null )
			{
				continue;
			}

			final Block blk = state.getBlock();
			if ( blk == null )
			{
				continue;
			}

			final Item what = Item.getItemFromBlock( blk );
			if ( what == null )
			{
				continue;
			}

			final String name = what.getItemStackDisplayName( new ItemStack( what, 1, blk.getMetaFromState( state ) ) );

			Integer count = contents.get( name );

			if ( count == null )
			{
				count = e.getValue();
			}
			else
			{
				count += e.getValue();
			}

			contents.put( name, count );
		}

		if ( contents.isEmpty() )
		{
			details.add( LocalStrings.Empty.getLocal() );
		}

		for ( final Entry<String, Integer> e : contents.entrySet() )
		{
			details.add( new StringBuilder().append( e.getValue() ).append( ' ' ).append( e.getKey() ).toString() );
		}
	}

	public int getSideFlags(
			final int minRange,
			final int maxRange,
			final int totalRequired )
	{
		int output = 0x00;

		for ( final EnumFacing face : EnumFacing.VALUES )
		{
			final int edge = face.getAxisDirection() == AxisDirection.POSITIVE ? 15 : 0;
			int required = totalRequired;

			switch ( face.getAxis() )
			{
				case X:
					for ( int z = minRange; z <= maxRange; z++ )
					{
						for ( int y = minRange; y <= maxRange; y++ )
						{
							if ( get( edge, y, z ) != 0 )
							{
								required--;
							}
						}
					}
					break;
				case Y:
					for ( int z = minRange; z <= maxRange; z++ )
					{
						for ( int x = minRange; x <= maxRange; x++ )
						{
							if ( get( x, edge, z ) != 0 )
							{
								required--;
							}
						}
					}
					break;
				case Z:
					for ( int y = minRange; y <= maxRange; y++ )
					{
						for ( int x = minRange; x <= maxRange; x++ )
						{
							if ( get( x, y, edge ) != 0 )
							{
								required--;
							}
						}
					}
					break;
				default:
					throw new NullPointerException();
			}

			if ( required <= 0 )
			{
				output |= 1 << face.ordinal();
			}
		}

		return output;
	}

	public boolean filter(
			final EnumWorldBlockLayer layer )
	{
		final HashMap<Integer, Boolean> filterState = new HashMap<Integer, Boolean>();
		boolean hasValues = false;

		for ( int x = 0; x < array_size; x++ )
		{
			final int ref = values[x];

			Boolean state = filterState.get( ref );
			if ( state == null )
			{
				filterState.put( ref, state = inLayer( layer, ref ) );
				hasValues = hasValues || state;
			}

			if ( state == false )
			{
				values[x] = 0;
			}
		}

		return hasValues;
	}

	private Boolean inLayer(
			final EnumWorldBlockLayer layer,
			final int ref )
	{
		final IBlockState state = Block.getStateById( ref );
		return state.getBlock().canRenderInLayer( layer );
	}
}
