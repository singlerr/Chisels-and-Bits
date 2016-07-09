package mod.chiselsandbits.blueprints;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.share.ShareWorldData;

public class BlueprintData implements Runnable
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
	private final URL url;

	private ShareWorldData data;

	public BlueprintData(
			final String url )
	{
		URL myURL = null;

		try
		{
			if ( url != null )
			{
				myURL = new URL( url );
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
		finally
		{
			this.url = myURL;
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
			if ( peek[0] == 0x89 && peek[1] == 0x50 && peek[2] == 0x4E && peek[3] == 0x47 )
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

	@Override
	public void run()
	{
		final EnumLoadState result = EnumLoadState.FAILED;
		try
		{
			final URLConnection src = url.openConnection();
			loadData( src.getInputStream() );
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
		return data.getStuctureData();
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

}
