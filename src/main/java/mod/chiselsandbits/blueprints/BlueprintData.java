package mod.chiselsandbits.blueprints;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.imageio.ImageIO;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.IVoxelAccess;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.share.ShareWorldData;
import mod.chiselsandbits.voxelspace.IVoxelProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class BlueprintData implements Runnable, IVoxelProvider
{
	public static enum EnumLoadState
	{
		LOADING,
		LOADED,
		FAILED;

		public boolean readyOrWaiting()
		{
			return this != FAILED;
		}
	};

	private long lastNeeded = getCurrentTime();
	private EnumLoadState state = EnumLoadState.LOADING;
	private URL url = null;

	private ShareWorldData data;

	public BlueprintData(
			final String url )
	{
		try
		{
			if ( url != null )
			{
				this.url = new URL( url );
				final Thread t = new Thread( this );
				t.setName( "Blueprint-" + url );
				t.start();
			}
		}
		catch ( final MalformedURLException e )
		{
			state = EnumLoadState.FAILED;
			Log.logError( "Blueprint URL is invalid.", e );
		}
	}

	private long getCurrentTime()
	{
		return System.currentTimeMillis();
	}

	synchronized public void updateTime()
	{
		lastNeeded = getCurrentTime();
	}

	synchronized public boolean isExpired()
	{
		return getCurrentTime() - lastNeeded > ChiselsAndBits.getConfig().blueprintExpireTime;
	}

	synchronized public EnumLoadState getState()
	{
		updateTime();
		return state;
	}

	public void setLocalSource(
			final String string ) throws MalformedURLException, UnsupportedEncodingException
	{
		url = new URL( "file", ClientSide.instance.getLocalName(), 0, "/" + URLEncoder.encode( string, "UTF-8" ) );
	}

	public void setURLSource(
			final URL url2 )
	{
		url = url2;
	}

	public void loadData(
			InputStream is ) throws IOException
	{
		is = new BufferedInputStream( is );
		EnumLoadState result = EnumLoadState.FAILED;

		try
		{
			final byte[] peek = new byte[4];
			is.mark( peek.length );
			is.read( peek );
			is.reset();

			// load png? or is it text?
			if ( peek[0] == (byte) 0x89 && peek[1] == 0x50 && peek[2] == 0x4E && peek[3] == 0x47 )
			{
				data = new ShareWorldData( ImageIO.read( is ) );
			}
			else
			{
				final StringBuilder builder = new StringBuilder();
				final byte[] buffer = new byte[2048];
				int read = 0;

				do
				{
					read = is.read( buffer );

					// C&B data should be visible as ascii if utf8 or various
					// iso
					// formats, probably the best approach to prevent utf-8 from
					// being confused from other charsets
					if ( read > 0 )
					{
						builder.append( new String( buffer, 0, read, "ASCII" ) );
					}
				}
				while ( read > 0 );

				data = new ShareWorldData( builder.toString() );
			}

			result = EnumLoadState.LOADED;
		}
		finally
		{
			state = result;
		}
	}

	public void loadData(
			final byte[] bs ) throws IOException
	{
		EnumLoadState result = EnumLoadState.FAILED;
		try
		{
			data = new ShareWorldData( bs );
			result = EnumLoadState.LOADED;
		}
		finally
		{
			state = result;
		}
	}

	@Override
	public void run()
	{
		EnumLoadState result = EnumLoadState.FAILED;
		try
		{
			if ( url.getProtocol().equals( "file" ) )
			{
				if ( url.getHost().equals( ClientSide.instance.getLocalName() ) )
				{
					loadData( new FileInputStream( new File( URLDecoder.decode( url.getFile().substring( 1 ), "UTF-8" ) ) ) );
					result = EnumLoadState.LOADED;
					return;
				}

				return;
			}

			final URLConnection src = url.openConnection();
			loadData( src.getInputStream() );
			result = EnumLoadState.LOADED;
		}
		catch ( final IOException e )
		{
			Log.logError( "Unload to download Blueprint.", e );
		}
		finally
		{
			state = result;
		}
	}

	public byte[] getStuctureData() throws IOException
	{
		if ( getState() == EnumLoadState.LOADED )
		{
			return data.getStuctureData();
		}

		throw new IOException();
	}

	public int getXSize()
	{
		return data.getXSize();
	}

	public int getYSize()
	{
		return data.getYSize();
	}

	public int getZSize()
	{
		return data.getZSize();
	}

	public String getURL()
	{
		return url == null ? "" : url.toString();
	}

	@Override
	public IVoxelAccess get(
			final int x,
			final int y,
			final int z )
	{
		return data.getBlob( x, y, z );
	}

	public IBlockState getStateAt(
			final BlockPos p )
	{
		return data.getStateAt( p );
	}

	public TileEntityBlockChiseled getTileAt(
			final BlockPos p )
	{
		return data.getTileAt( p );
	}

}
