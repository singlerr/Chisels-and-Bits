package mod.chiselsandbits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log
{

	private Log()
	{

	}

	private static Logger getLogger()
	{
		return LogManager.getLogger( ChiselsAndBits.MODID );
	}

	public static void logError(
			final String message,
			final Throwable e )
	{
		getLogger().error( message, e );
	}

	public static void info(
			final String message )
	{
		getLogger().info( message );
	}

}
