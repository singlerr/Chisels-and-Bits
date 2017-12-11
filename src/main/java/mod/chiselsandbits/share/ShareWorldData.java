package mod.chiselsandbits.share;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.lang3.StringEscapeUtils;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.IVoxelAccess;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.chiseledblock.serialization.StringStates;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class ShareWorldData
{

	public static class SharedWorldBlock
	{
		public SharedWorldBlock(
				final byte[] bytes )
		{
			if ( bytes.length > 0 && bytes[0] == 1 ) // block
			{
				blob = null;
				isBlob = false;
				blockName = new String( bytes, 1, bytes.length - 1 );
			}
			else if ( bytes.length > 0 && bytes[0] == 2 ) // blob )
			{
				isBlob = true;
				blockName = null;

				final byte[] tmp = new byte[bytes.length - 1];
				System.arraycopy( bytes, 1, tmp, 0, tmp.length );
				blob = new VoxelBlobStateReference( tmp, 0 );
			}
			else
			{
				isBlob = false;
				blob = null;
				blockName = "minecraft:air";
			}
		}

		final boolean isBlob;

		final String blockName;
		final VoxelBlobStateReference blob;

		public IBlockState getState()
		{
			return ModUtil.getStateById( StringStates.getStateIDFromName( blockName ) );
		}
	};

	private int xSize;
	private int ySize;
	private int zSize;
	private int xySize;

	int[] blocks;
	SharedWorldBlock[] models;

	public int getXSize()
	{
		return xSize;
	}

	public int getYSize()
	{
		return ySize;
	}

	public int getZSize()
	{
		return zSize;
	}

	public ShareWorldData(
			String data ) throws IOException
	{
		final String header = "[C&B](";
		final String footer = ")[C&B]";
		final String htmlHeader = "[C&amp;B]";

		// is it html encoded? if it is decode the page before moving on.
		if ( data.indexOf( htmlHeader ) != -1 )
		{
			data = StringEscapeUtils.unescapeHtml4( data );
		}

		int start = -1;
		int end = -1;

		// find a valid pattern in side...
		Pattern p = Pattern.compile( "\\[C&B\\]\\([A-Za-z0-9+/=\n\r ]+\\)\\[C&B\\]" );
		Matcher m = p.matcher( data );
		if ( m.find() )
		{
			start = m.start();
			end = m.end();
		}

		if ( start == -1 || end == -1 )
		{
			throw new IOException( "Unable to locate C&B Data." );
		}

		start += header.length();
		data = data.substring( start, end - footer.length() );
		final byte[] compressed = Base64.getDecoder().decode( data );
		readCompressed( compressed );
	}

	public ShareWorldData(
			final byte[] compressed ) throws IOException
	{
		readCompressed( compressed );
	}

	public ShareWorldData(
			final BufferedImage img ) throws IOException
	{
		final byte[] data = ( (DataBufferByte) img.getRaster().getDataBuffer() ).getData();

		final ScreenshotDecoder sdecoder = new ScreenshotDecoder();
		final byte[] compressed = sdecoder.imageDecode( data );

		readCompressed( compressed );
	}

	private void readCompressed(
			final byte[] compressed ) throws IOException
	{
		byte[] uncompressed = null;

		final InflaterInputStream in = new InflaterInputStream( new ByteArrayInputStream( compressed ) );
		final ByteArrayOutputStream bout = new ByteArrayOutputStream( compressed.length );

		int b;
		while ( ( b = in.read() ) != -1 )
		{
			bout.write( b );
		}
		bout.close();
		in.close();

		uncompressed = bout.toByteArray();

		final ShareFormatReader reader = new ShareFormatReader( uncompressed );

		final int format = reader.readInt();

		if ( format != 1 )
		{
			throw new IOException( "Invalid format!" );
		}

		xSize = reader.readInt();
		ySize = reader.readInt();
		zSize = reader.readInt();
		xySize = xSize * ySize;

		final int bits = reader.readInt();

		blocks = new int[xSize * ySize * zSize];
		for ( int x = 0; x < blocks.length; x++ )
		{
			blocks[x] = reader.readBits( bits );
		}

		reader.snapToByte();

		final int modelCount = reader.readInt();
		models = new SharedWorldBlock[modelCount];
		for ( int x = 0; x < models.length; x++ )
		{
			models[x] = new SharedWorldBlock( reader.readBytes() );
		}

		structureData = new byte[Math.min( reader.consumedBytes(), uncompressed.length )];
		System.arraycopy( uncompressed, 0, structureData, 0, structureData.length );
	}

	private byte[] structureData;

	public byte[] getStuctureData() throws IOException
	{
		final ByteArrayOutputStream byteStream = new ByteArrayOutputStream( structureData.length );

		try
		{
			final DeflaterOutputStream zipStream = new DeflaterOutputStream( byteStream );
			try
			{
				zipStream.write( structureData );
			}
			finally
			{
				zipStream.close();
			}
		}
		finally
		{
			byteStream.close();
		}

		return byteStream.toByteArray();
	}

	public IVoxelAccess getBlob(
			final int x,
			final int y,
			final int z )
	{
		final SharedWorldBlock swb = getAtPos( x, y, z );

		if ( swb != null )
		{
			if ( swb.blob == null )
			{
				final IBlockState bs = swb.getState();
				final int stateID = Block.getStateId( bs );
				if ( BlockBitInfo.supportsBlock( bs ) )
				{
					return new VoxelBlobStateReference( stateID, 0 );
				}
				return new VoxelBlobStateReference( 0, 0 );
			}

			return swb.blob;
		}

		return new VoxelBlobStateReference( 0, 0 );
	}

	SharedWorldBlock getAtPos(
			final int x,
			final int y,
			final int z )
	{
		if ( x >= 0 && y >= 0 && z >= 0 && x < xSize && y < ySize && z < zSize )
		{
			final int modelid = blocks[x + y * xSize + z * xySize];
			if ( models.length > modelid && modelid >= 0 )
			{
				return models[modelid];
			}
		}

		return null;
	}

	public IBlockState getStateAt(
			final BlockPos p )
	{
		final SharedWorldBlock swb = getAtPos( p.getX(), p.getY(), p.getZ() );

		if ( swb != null )
		{
			if ( swb.blob != null )
			{
				return ChiselsAndBits.getBlocks().getChiseledDefaultState();
			}
			else
			{
				return swb.getState();
			}
		}

		return Blocks.AIR.getDefaultState();
	}

	public TileEntityBlockChiseled getTileAt(
			final BlockPos p )
	{
		final SharedWorldBlock swb = getAtPos( p.getX(), p.getY(), p.getZ() );

		if ( swb != null )
		{
			if ( swb.blob != null )
			{
				final TileEntityBlockChiseled te = new TileEntityBlockChiseled();
				te.setState( te.getBasicState().withProperty( BlockChiseled.UProperty_VoxelBlob, swb.blob ) );
				return te;
			}
		}

		return null;
	}

}
