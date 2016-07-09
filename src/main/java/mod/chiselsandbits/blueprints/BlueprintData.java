package mod.chiselsandbits.blueprints;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;

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

	public BlueprintData(
			final String url )
	{
		URL myURL = null;

		try
		{
			myURL = new URL( url );
			final Thread t = new Thread( this );
			t.setName( "Blueprint-" + url );
			t.start();
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

	@Override
	public void run()
	{
		final EnumLoadState result = EnumLoadState.FAILED;
		try
		{
			final URLConnection src = url.openConnection();
			src.getInputStream();

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

}
